package com.andycugb.cron.zk;

import com.andycugb.cron.util.Constant;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jbcheng on 2016-03-16.
 */
public class ZooKeeperSupport {
    private static volatile ZooKeeper zooKeeper = null;
    private static final Object zookeeperLock = new Object();
    public static boolean isUseZK = true;

    /**
     * get zk client,create a new one when necessary
     * @return zk client
     */
    public static ZooKeeper getZooKeeper() {
        if (zooKeeper == null || !zooKeeper.getState().isAlive()) {
            synchronized (zookeeperLock) {
                if (isUseZK && (zooKeeper == null || !zooKeeper.getState().isAlive())) {
                    try {
                        zooKeeper = createNewZooKeeper();
                    } catch (Exception e) {
                        Constant.log_cron.error("[initZKConfig] error happens when create new:"
                                + e);
                    }
                }
            }
        }
        return zooKeeper;
    }

    /**
     * create new zk client
     * @return zk client
     * @throws Exception
     */
    private static ZooKeeper createNewZooKeeper() throws Exception {
        Constant.log_cron.info("[createNewZooKeeper] create new zk instance start...");
        //wait for zk connect->Watcher.Event.KeeperState.SyncConnected
        CountDownLatch connectedLatch = new CountDownLatch(1);
        ZooKeeper zooKeeper =
                new ZooKeeper(ZooKeeperConfig.getInstance().getConnectUrl(), ZooKeeperConfig.getInstance()
                        .getTimeout(), new CountDownWatcher(connectedLatch));
        if (ZooKeeper.States.CONNECTING == zooKeeper.getState()) {
            Constant.log_cron.info("[createNewZooKeeper] new instance,wait for state alive.");
            //check when timeout give up zk lock
            boolean ret =
                    connectedLatch.await(ZooKeeperConfig.getInstance().getTimeout(),
                            TimeUnit.MICROSECONDS);
            if (!ret) {
                isUseZK = false;
                Constant.log_cron
                        .error("[createNewZookeeper] Can\'t connect to ZK ,please check , now the system will run without ZK.");
            }
        }
        Constant.log_cron.info("[createNewZookeeper] create new instance finish.");
        return zooKeeper;
    }
}
