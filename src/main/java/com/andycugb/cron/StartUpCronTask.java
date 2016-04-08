package com.andycugb.cron;

import com.andycugb.cron.db.CronJobModel;

/**
 * Created by jbcheng on 2016-03-18.
 */
public class StartUpCronTask implements Runnable {

    private CronJobModel model;

    public StartUpCronTask(CronJobModel model) {
        this.model = model;
    }

    public void run() {

    }
}
