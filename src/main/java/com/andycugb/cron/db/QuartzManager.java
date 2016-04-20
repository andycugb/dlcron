package com.andycugb.cron.db;


import com.andycugb.cron.ClassGenerator;
import com.andycugb.cron.CronDeployException;
import com.andycugb.cron.StartUpCronTask;
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

    private QuartzManager() {

    }

    static class QuartzManagerHolder {
        private final static QuartzManager QUARTZ_MANAGER = new QuartzManager();
    }

    public static QuartzManager getInstance() {
        return QuartzManagerHolder.QUARTZ_MANAGER;
    }

    // init scheduler
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

    /**
     * reset scheduler cron jobs
     * 
     * @param cronModel job map
     */
    public void reloadCronModels(Map<String, CronJobModel> cronModel) {
        this.cronModel = cronModel;
    }

    public Scheduler getScheduler() {
        this.initScheduler();
        return this.scheduler;
    }

    /**
     * start scheduler,if not start
     */
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

    /**
     * get cron job by name
     * 
     * @param jobName cron job name
     * @return cron
     */
    public CronJobModel getJobByName(String jobName) {
        return this.cronModel.get(jobName);
    }

    /**
     * add new cron job to scheduler
     * 
     * @param cron cron to be added
     * @return add status
     */
    public boolean addNewJob(CronJobModel cron) {
        if (1 == cron.getFireOnStartUp()) {
            ThreadPool.getInstance().exec(new StartUpCronTask(cron));
        }
        this.initScheduler();
        boolean success = false;
        Class clazz = this.classGenerator.getClazz(cron);
        if (clazz != null) {
            JobDetailImpl job = new JobDetailImpl();
            job.setName(cron.getCronName());
            job.setJobClass(clazz);

            try {
                CronTriggerImpl trigger = new CronTriggerImpl();
                trigger.setName(cron.getCronName());
                trigger.setCronExpression(cron.getCronExpression());

                this.scheduler.scheduleJob(job, trigger);
            } catch (ParseException e) {
                Constant.LOG_CRON.error(
                        "Fail to start cron system: Fail to deploying cron, cron="
                                + cron.toString(), e);
            } catch (SchedulerException e) {
                Constant.LOG_CRON.error(
                        "Fail to start cron system: Fail to deploying cron, cron="
                                + cron.toString(), e);
            }
        }
        return success;
    }

    /**
     * delete cron job by name
     * 
     * @param jobName cron job name
     */
    public void deleteJob(String jobName) throws CronDeployException {
        this.initScheduler();
        try {
            this.scheduler.deleteJob(new JobKey(jobName));
            Constant.LOG_CRON.info("[Deploy]Success to delete an old cron, " + jobName);
        } catch (SchedulerException e) {
            throw new CronDeployException(e);
        }
    }
}
