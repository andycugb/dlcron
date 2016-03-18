package com.andycugb.cron.zk;

import com.andycugb.cron.CronTask;
import com.andycugb.cron.util.Constant;
import com.andycugb.cron.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jbcheng on 2016-03-17.
 */
public class ZooKeeperLock implements Watcher {
    private ZooKeeper zooKeeper;
    private String path;
    private String jobName;
    private String callType;
    private Timestamp runTime;
    private String currentNode;
    private boolean isDone;
    private boolean isUseDB;
    private CronTask cronTask;
    private final Object lock = new Object();

    private final String doing = "doing";
    private final String done = "done";
    private final String logPrefix = "Job[" + this.jobName + "][" + this.callType + "]["
            + DateUtil.format(this.runTime) + "]";

    public ZooKeeperLock(String rootPath, String jobName, String callType, Timestamp runTime,
            CronTask cronTask, boolean isUseDB) {
        this.path = rootPath;
        this.jobName = jobName;
        this.callType = callType;
        this.runTime = runTime;
        this.cronTask = cronTask;
        this.isUseDB = isUseDB;
        zooKeeper = ZooKeeperSupport.getZooKeeper();

        if (zooKeeper != null) {
            String[] paths = rootPath.split("/");
            StringBuilder sb = new StringBuilder("/");
            int index = 0;
            for (int n = paths.length; index < n; index++) {
                if (StringUtils.isNotBlank(path)) {
                    sb.append(path);
                    try {
                        Stat stat = this.zooKeeper.exists(sb.toString(), this);
                        if (stat == null) {
                            byte[] data = new byte[0];
                            if (index == n - 1) { // mark the last node data
                                data = doing.getBytes();
                            }
                            this.zooKeeper.create(sb.toString(), data,
                                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                        }
                    } catch (KeeperException.NodeExistsException e) {
                        Constant.LOG_CRON.warn(logPrefix + "[path=" + sb.toString()
                                + "]NodeExists while instantiating locks object." + e);
                    } catch (Exception e) {
                        Constant.LOG_CRON.error(logPrefix + "[path=" + sb.toString()
                                + "]Exception while instantiating locks object." + e);
                        throw new RuntimeException(e);
                    }
                    sb.append("/");
                }
            }
        }
    }

    /**
     * get the status of current logic thread 1、exit when already been done 2、could be continue when
     * next condition occurs 2。1：do not get lock,wait until been signal 2.2：continue execute real
     * logic and modify status
     * 
     * @return check status
     * @throws InterruptedException
     * @throws KeeperException
     */
    public Map<String, Object> checkStatus() throws InterruptedException, KeeperException {
        Map<String, Object> lockStatus = new HashMap<String, Object>(2);
        try {
            this.currentNode =
                    this.zooKeeper.create(
                            this.path + "/lock_",
                            Constant.SERVER_IP == null ? new byte[0] : Constant.SERVER_IP
                                    .getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.EPHEMERAL_SEQUENTIAL);
            lockStatus = this.getLock();
        } catch (KeeperException.NoNodeException e) {
            lockStatus.put("code", 200);
            lockStatus.put("desc", logPrefix + "has been done," + this.path);
        }
        return lockStatus;
    }

    private Map<String, Object> getLock() throws InterruptedException, KeeperException {
        Map<String, Object> lockStatus = new HashMap<String, Object>(2);
        if (this.isDone()) { // quit when already done
            Constant.LOG_CRON.info("Job[" + this.jobName + "][" + this.callType
                    + "]has already done," + this.path);
            lockStatus.put("code", Constant.CronJobStatus.SUCCESS);
            lockStatus.put("desc", logPrefix + "has been done," + this.path);
            return lockStatus;
        } else {
            List<String> children = this.zooKeeper.getChildren(this.path, false);
            String[] nodes = children.toArray(new String[children.size()]);

            Arrays.sort(nodes);// check whether get lock by sorted value

            if (this.currentNode.equals(this.path + "/" + nodes[0])) {
                Constant.LOG_CRON.info(logPrefix + "get lock, start to do action, " + this.path
                        + "/" + nodes[0]);
                return this.doAction();
            } else {
                Constant.LOG_CRON
                        .info(logPrefix + "wait for lock, " + this.path + "/" + nodes[0]);
                return this.waitForLock(nodes[0]);
            }
        }
    }

    private synchronized boolean isDone() {
        if (!this.isDone) {
            try {
                Stat stat = this.zooKeeper.exists(this.path, this);
                if (stat != null) {
                    String status = new String(this.zooKeeper.getData(this.path, this, stat));
                    if (done.equalsIgnoreCase(status)) {
                        this.isDone = true;
                    }
                }
            } catch (Exception e) {
                Constant.LOG_CRON.error(logPrefix + "Exception occurs while doing isDone" + e);

            }
        }
        return this.isDone;
    }

    private Map<String, Object> doAction() throws InterruptedException, KeeperException {
        // execute real business logic
        Map<String, Object> result =
                this.cronTask.doTask(this.jobName, this.callType, this.runTime, this.isUseDB);
        try {
            Stat stat = this.zooKeeper.exists(this.path, false);
            if (stat != null) {
                this.zooKeeper.setData(this.path, done.getBytes(), stat.getVersion());
            }
        } catch (Exception e) {
            Constant.LOG_CRON.error(logPrefix + "Exception occurs while doing doAction" + e);
        }
        return result;
    }

    private Map<String, Object> waitForLock(String node) throws InterruptedException,
            KeeperException {
        Stat stat = this.zooKeeper.exists(this.path + "/" + node, this);
        if (stat != null) {
            synchronized (lock) {
                this.lock.wait();
            }
        }
        return this.getLock();
    }

    /**
     * listen node delete and data change event
     * 
     * @param event
     */
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeDeleted) {
            // signal wait thread
            synchronized (lock) {
                Constant.LOG_CRON.info(logPrefix + "notified, event=" + event.getType()
                        + ", path=" + event.getPath());

                this.lock.notify();
            }
        } else if (event.getType() == Event.EventType.NodeDataChanged
                && this.path.equalsIgnoreCase(event.getPath()) && this.isDone) {
            synchronized (lock) {
                Constant.LOG_CRON.info(logPrefix + "notified, event=" + event.getType()
                        + ", path=" + event.getPath());

                this.lock.notify();
            }
            // delete lock node
            try {
                this.deleteNode(this.currentNode);
                List<String> children = this.zooKeeper.getChildren(this.path, false);
                if (CollectionUtils.isNotEmpty(children)) {
                    this.deleteNode(this.path);
                }
            } catch (Exception e) {
                Constant.LOG_CRON.info("delete node error " + e);
            }
        }
    }

    private void deleteNode(String path) {
        int retry = 2;
        boolean isDeleted = false;

        while (!isDeleted && retry-- > 0) {
            try {
                this.zooKeeper.delete(path, -1);
                isDeleted = true;
            } catch (Exception ex) {
                Constant.LOG_CRON.info("delete node error : " + path + " by : "
                        + ex.getClass().getName() + "[" + ex.getMessage()
                        + "], waiting for deleteOldNode");
            }
        }
    }
}
