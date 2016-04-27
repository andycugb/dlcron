package com.andycugb.cron.zk;

import com.andycugb.cron.util.Constant;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jbcheng on 2016-03-16.
 */
public class ZooKeeperSupport {
    private static final Object zookeeperLock = new Object();
    public static boolean isUseZK = true;
    private static volatile ZooKeeper zooKeeper = null;

    /**
     * get zk client,create a new one when necessary.
     * 
     * @return zk client
     */
    public static ZooKeeper getZooKeeper() {
        if (zooKeeper == null || !zooKeeper.getState().isAlive()) {
            synchronized (zookeeperLock) {
                if (isUseZK && (zooKeeper == null || !zooKeeper.getState().isAlive())) {
                    try {
                        zooKeeper = createNewZooKeeper();
                    } catch (Exception e) {
                        Constant.LOG_CRON.error("[initZKConfig] error happens when create new:"
                                + e);
                    }
                }
            }
        }
        return zooKeeper;
    }

    public static void setZookeeper(ZooKeeper zookeeper) {
        ZooKeeperSupport.zooKeeper = zookeeper;
    }

    /**
     * create new zk client,when zk start,it will creates two async threads,we should wait until the
     * real connect being ok.
     * 
     * @return zk client
     * @throws Exception thread interrupted
     */
    public static ZooKeeper createNewZooKeeper() throws Exception {
        Constant.LOG_CRON.info("[createNewZooKeeper] create new zk instance start...");
        // wait for zk connect->Watcher.Event.KeeperState.SyncConnected
        CountDownLatch connectedLatch = new CountDownLatch(1);
        ZooKeeper zooKeeper =
                new ZooKeeper(ZooKeeperConfig.getInstance().getConnectUrl(), ZooKeeperConfig
                        .getInstance().getTimeout(), new CountDownWatcher(connectedLatch));
        if (ZooKeeper.States.CONNECTING == zooKeeper.getState()) {
            Constant.LOG_CRON.info("[createNewZooKeeper] new instance,wait for state alive.");
            // check when timeout give up zk lock
            boolean ret =
                    connectedLatch.await(ZooKeeperConfig.getInstance().getTimeout(),
                            TimeUnit.MICROSECONDS);
            if (!ret) {
                isUseZK = false;
                Constant.LOG_CRON
                        .error("[createNewZookeeper] Can\'t connect to ZK ,please check"
                                +
                                " , now the system will run without ZK.");
            }
        }
        Constant.LOG_CRON.info("[createNewZookeeper] create new instance finish.");
        return zooKeeper;
    }
}
