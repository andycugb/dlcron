package com.andycugb.cron;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jbcheng on 2016-03-18.
 */
public class JobContainer {
    private static final ConcurrentHashMap<String, Boolean> runningCronJob =
            new ConcurrentHashMap<String, Boolean>();

    /**
     * check whether the service is running.
     * 
     * @param jobName service name
     * @return service running status
     */
    public static synchronized boolean runAndGetLock(String jobName) {
        Boolean running = runningCronJob.get(jobName);
        if (running == null || !running) {
            runningCronJob.putIfAbsent(jobName, true);
            return false;
        }
        return true;
    }

    /**
     * when service done modify it`s status.
     * 
     * @param jobName service name
     */
    public static synchronized void finished(String jobName) {
        runningCronJob.put(jobName, Boolean.FALSE);
    }
}
