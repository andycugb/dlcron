package com.andycugb.cron.db;


import com.andycugb.cron.CronDeployException;
import com.andycugb.cron.StartUpCronTask;
import com.andycugb.cron.util.ClassGenerator;
import com.andycugb.cron.util.Constant;
import com.andycugb.cron.util.ThreadPool;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jbcheng on 2016-03-18.
 */
@Repository
public class QuartzManager {
    private Scheduler scheduler;
    private Map<String, CronJobModel> cronModel = new HashMap<String, CronJobModel>();
    private ClassGenerator classGenerator = ClassGenerator.getInstance();

    private void initScheduler() {
        if (this.scheduler == null) {
            try {
                this.scheduler = new StdSchedulerFactory().getScheduler();
            } catch (SchedulerException e) {
                Constant.LOG_CRON.error("Can\'t instantiate scheduler, ", e);
                throw new CronDeployException(e);
            }
        }
    }


    public Map<String, CronJobModel> geAllCronModels() {
        return cronModel;
    }

    public void reloadCronModels(Map<String, CronJobModel> cronModel) {
        this.cronModel = cronModel;
    }

    public Scheduler getScheduler() {
        this.initScheduler();
        return this.scheduler;
    }

    public void startScheduler() {
        try {
            this.scheduler = getScheduler();
            if (!this.scheduler.isStarted()) {
                this.scheduler.start();
            }
        } catch (SchedulerException e) {
            Constant.LOG_CRON.error("Can\'t start scheduler, " + e);
            throw new CronDeployException(e);
        }
    }

    public CronJobModel getJobByName(String jobName) {
        return this.cronModel.get(jobName);
    }

    public boolean addNewJob(CronJobModel model) {
        if (1 == model.getFireOnStartUp()) {
            ThreadPool.getInstance().exec(new StartUpCronTask(model));
        }
        this.initScheduler();
        boolean success = false;
        Class clazz = this.classGenerator.getClazz(model.getCronName(), model.getServiceName());
        if (clazz != null) {
            JobDetailImpl job = new JobDetailImpl();
            job.setName(model.getCronName());
            job.setJobClass(clazz);

            try {
                CronTriggerImpl trigger = new CronTriggerImpl();
                trigger.setName(model.getCronName());
                trigger.setCronExpression(model.getCronExpression());

                this.scheduler.scheduleJob(job, trigger);
            } catch (ParseException e) {
                Constant.LOG_CRON.error(
                        "Fail to start cron system: Fail to deploying cron, cron="
                                + model.toString(), e);
            } catch (SchedulerException e) {
                Constant.LOG_CRON.error(
                        "Fail to start cron system: Fail to deploying cron, cron="
                                + model.toString(), e);
            }
        }
        return success;
    }

    public void deleteJob(String jobName) {
        this.initScheduler();
        try {
            this.scheduler.deleteJob(new JobKey(jobName));
            Constant.LOG_CRON.info("[Deploy]Success to delete an old cron, " + jobName);
        } catch (SchedulerException e) {
            throw new CronDeployException(e);
        }
    }
}
