package com.andycugb.cron;

import com.andycugb.cron.db.CronJobModel;
import com.andycugb.cron.util.Constant;

/**
 * Created by jbcheng on 2016-03-18.
 */
public class StartUpCronTask implements Runnable {

    private CronJobModel cron;

    public StartUpCronTask(CronJobModel model) {
        this.cron = model;
    }

    /**
     * invoke given cron method.
     */
    public void run() {
        try {
            long start = System.currentTimeMillis();
            String result = ClassGenerator.getInstance().executeJob(this.cron, "start", true);
            long end = System.currentTimeMillis();
            Constant.LOG_CRON.info("Success to fire Job[" + this.cron.getCronName()
                    + "] on startup, result={" + result + "}, cost_time=" + (end - start) + "ms");
        } catch (Exception e) {
            Constant.LOG_CRON.error(
                    "Exception occurs while firing Job[" + this.cron.getCronName()
                            + "] on startup", e);
        }
    }
}
