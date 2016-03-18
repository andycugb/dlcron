package com.andycugb.cron.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jbcheng on 2016-03-18.
 */
public class ThreadPool {
    private ExecutorService exec;

    private ThreadPool() {
        // set max blocking queue size at 1000,abort request if exceed
        exec =
                new ThreadPoolExecutor(5, 20, 300L, TimeUnit.SECONDS,
                        new ArrayBlockingQueue<Runnable>(1000),
                        new ThreadPoolExecutor.AbortPolicy());
    }

    static class ThreadPoolHolder {
        private static ThreadPool INSTANCE = new ThreadPool();
    }

    public static ThreadPool getInstance() {
        return ThreadPoolHolder.INSTANCE;
    }

    public void exec(Runnable cmd) {
        this.exec.execute(cmd);
    }

}
