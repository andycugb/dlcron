package com.andycugb.cron;

import com.andycugb.cron.db.CronJobModel;

/**
 * Created by jbcheng on 2016-03-18.
 */
public class StartUpCronTask implements Runnable{

    private CronJobModel model;
    public StartUpCronTask(CronJobModel model){
        this.model=model;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run() {

    }
}
