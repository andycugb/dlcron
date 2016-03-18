package com.andycugb.cron.zk;

import com.andycugb.cron.util.Constant;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

/**
 * Created by jbcheng on 2016-03-16.
 */
public class CountDownWatcher implements Watcher {

    private CountDownLatch connectedLatch;

    public CountDownWatcher() {

    }

    public CountDownWatcher(CountDownLatch connectedLatch) {
        this.connectedLatch = connectedLatch;
    }

    public void process(WatchedEvent watchedEvent) {
        if (connectedLatch != null && watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            this.connectedLatch.countDown();
        }
        Constant.LOG_CRON.info("[DefaultWatcher] invoke by event:" + watchedEvent);
    }
}
