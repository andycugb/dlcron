package com.andycugb.cron;

import com.andycugb.cron.db.CronJobDao;
import com.andycugb.cron.db.CronJobModel;
import com.andycugb.cron.db.QuartzManager;
import com.andycugb.cron.util.Constant;
import com.andycugb.cron.util.DateUtil;
import com.andycugb.cron.util.PropertyUtil;
import com.andycugb.cron.zk.ZooKeeperConfig;
import com.andycugb.cron.zk.ZooKeeperLock;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.impl.JobDetailImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jbcheng on 2016-03-17.
 */
public abstract class AbstractCronJob implements Job, CronTask {

    @Autowired
    private CronJobDao cronJobJdbc;
    @Autowired
    private QuartzManager quartzManager;

    public abstract String doJob();

    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = ((JobDetailImpl) context.getJobDetail()).getName();
        Constant.LOG_CRON.info("[execute]Job[" + jobName + "]  begin to run, notify!");
        Timestamp runTime = DateUtil.toTimeStamp(context.getScheduledFireTime());
        this.checkWhenDoTask(jobName, "quartz", true, runTime);
        Constant.LOG_CRON.info("[execute]Job[" + jobName + "]  finish to run, notify!");
    }

    /**
     * check and do scheduled task
     * 
     * @param jobName service to do
     * @param callType execute type quartz or hand-set
     * @param hasLimitIp check ip limit when do quartz service
     * @param runTime execute time of service
     * @return desc of service
     */
    public String checkWhenDoTask(String jobName, String callType, boolean hasLimitIp,
            Timestamp runTime) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            if (JobContainer.runAndGetLock(jobName)) {
                Constant.LOG_CRON.info("Job[" + jobName + "][" + callType + "]["
                        + DateUtil.format(runTime)
                        + "] is running, task is canceled for this time.");
                return "Job[" + jobName + "][" + callType
                        + "] is running, task is canceled for this time.";
            } else {
                CronJobModel model = quartzManager.getJobByName(jobName);
                int runType = model.getRunType(Constant.SERVER_IP);// if limitIps contains local
                                                                   // ip,also
                // can exec
                boolean singleCheck = false;// TODO:how to avoid repeat task exec????
                boolean useZK = ZooKeeperConfig.getInstance().isUseZK();
                if (hasLimitIp) {
                    if (useZK) {
                        if (runType == Constant.RunType.RUN_ON_ALL) {
                            result = this.doTask(jobName, callType, runTime, singleCheck);
                        } else {
                            if (runType == Constant.RunType.RUN_ON_LOCAL) {
                                String prop =
                                        PropertyUtil.getStringProperty(Constant.CRON_SINGLE_CHECK);
                                if (Boolean.valueOf(prop)) {
                                    singleCheck = true;
                                }
                                result =
                                        this.doTaskWithZK(jobName, callType, runTime, singleCheck);
                                if (result.get(Constant.RETURN_CODE) != null
                                        && ((Integer) result.get(Constant.RETURN_CODE)) != Constant.CronJobStatus.SUCCESS
                                        && ((Integer) result.get(Constant.RETURN_CODE)) != Constant.CronJobStatus.ERROR) {
                                    // make sure this job will trigger,in case of zk link failed
                                    result = this.doTask(jobName, callType, runTime, singleCheck);
                                }
                            }
                        }
                    } else {
                        if (runType == Constant.RunType.RUN_ON_LOCAL
                                || runType == Constant.RunType.RUN_ON_ALL) {
                            result = this.doTask(jobName, callType, runTime, singleCheck);
                        }
                    }
                } else { // exec cron job by hand-set
                    if (useZK) {
                        if (runType == Constant.RunType.RUN_ON_ALL
                                || runType == Constant.RunType.RUN_ON_NONE) {
                            result = this.doTask(jobName, callType, runTime, singleCheck);
                        } else { // only select one server to exec job
                            result = this.doTaskWithZK(jobName, callType, runTime, singleCheck);
                            if (result.get(Constant.RETURN_CODE) != null
                                    && ((Integer) result.get(Constant.RETURN_CODE)) != Constant.CronJobStatus.SUCCESS
                                    && ((Integer) result.get(Constant.RETURN_CODE)) != Constant.CronJobStatus.ERROR) {
                                // make sure this job will trigger,in case of zk link fail
                                if (runType == Constant.RunType.RUN_ON_LOCAL) {
                                    result = this.doTask(jobName, callType, runTime, singleCheck);
                                }
                            }
                        }
                    } else {
                        result = this.doTask(jobName, callType, runTime, singleCheck);
                    }
                }
            }
        } catch (Exception e) {
            String desc =
                    "Job[" + jobName + "][" + callType + "][" + DateUtil.format(runTime)
                            + "]Exception occurs while doing job, " + e.toString();
            Constant.LOG_CRON.error(desc, e);
            result.put(Constant.RETURN_DESC, desc);
        } finally {
            JobContainer.finished(jobName);
        }
        return String.valueOf(result.get(Constant.RETURN_DESC));
    }

    /**
     * use zk lock to exec cron job,in this case,should retry to make sure trigger been done
     * 
     * @param jobName job`s name
     * @param callType exec type
     * @param runTime exec time
     * @param isUseDB use db lock to avoid repeat trigger
     * @return
     */
    public Map<String, Object> doTaskWithZK(String jobName, String callType, Timestamp runTime,
            boolean isUseDB) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        try {
            String execTime = DateUtil.getExecTime(runTime);
            ZooKeeperConfig zkConfig = ZooKeeperConfig.getInstance();
            String root = zkConfig.getRoot();
            String product = zkConfig.getProduct();
            ZooKeeperLock lock =
                    new ZooKeeperLock(root + "/" + product + "/" + jobName + "/" + execTime,
                            jobName, callType, runTime, this, isUseDB);
            result = lock.checkStatus();
        } catch (Exception e) {
            String desc =
                    "Exception occurs while doing job[" + jobName + "][" + callType + "]["
                            + DateUtil.format(runTime) + "]";
            result.put(Constant.RETURN_DESC, desc);
            result.put(Constant.RETURN_CODE, Constant.CronJobStatus.RETRY);
            Constant.LOG_CRON.error(desc, e);
        }
        return result;
    }

    public Map<String, Object> doTask(String jobName, String callType, Timestamp runTime,
            boolean isUseDB) {
        Map<String, Object> result = new HashMap<String, Object>(2);

        Connection conn = null;
        try {
            try {
                if (isUseDB) {
                    conn = cronJobJdbc.getDataSource().getConnection();
                    Constant.LOG_CRON.debug("Job[" + jobName + "][" + callType + "]["
                            + DateUtil.format(runTime) + "] are going to get DB lock!");
                    CronJobModel model = cronJobJdbc.getCronByName(conn, jobName);
                    Constant.LOG_CRON.debug("Job[" + jobName + "][" + callType + "]["
                            + DateUtil.format(runTime) + "] have got DB lock!");
                    if (model == null) {
                        Constant.LOG_CRON.warn("Job[" + jobName + "][" + callType + "]["
                                + DateUtil.format(runTime)
                                + "] not exists，cancelled for this time");
                        result.put(Constant.RETURN_CODE, Constant.CronJobStatus.SUCCESS);
                        result.put(Constant.RETURN_DESC, "Job[" + jobName + "][" + callType
                                + "][" + DateUtil.format(runTime)
                                + "] not exists，cancelled for this time");
                        return result;
                    }

                    if (model.getLastRunTime() != null
                            && DateUtil.getInterValSeconds(model.getLastRunTime(), runTime) < 1) {
                        Constant.LOG_CRON.warn("Job[" + jobName + "][" + callType + "]["
                                + DateUtil.format(runTime) + "] has been fired on "
                                + DateUtil.format(model.getLastRunTime())
                                + ", cancelled for this time");
                        result.put(Constant.RETURN_CODE, Constant.CronJobStatus.SUCCESS);
                        result.put(Constant.RETURN_DESC, "Job[" + jobName + "][" + callType
                                + "][" + DateUtil.format(runTime) + "] has been fired on "
                                + DateUtil.format(model.getLastRunTime())
                                + ", canceled for this time");
                        return result;
                    }
                }

                Constant.LOG_CRON.info("Job[" + jobName + "][" + callType + "]["
                        + DateUtil.format(runTime) + "] started");
                long start = System.currentTimeMillis();

                // real business logic
                String desc = this.doJob();
                result.put(Constant.RETURN_DESC, desc);
                result.put(Constant.RETURN_CODE, Constant.CronJobStatus.SUCCESS);

                long end = System.currentTimeMillis();
                Constant.LOG_CRON.info("Job[" + jobName + "][" + callType + "]["
                        + DateUtil.format(runTime) + "] finished, result={" + desc + "}, costs="
                        + (end - start) + "ms");
            } catch (Throwable e) {
                Constant.LOG_CRON.error("Exception occurs while doing service[" + jobName + "]["
                        + callType + "][" + DateUtil.format(runTime) + "]" + e);
                result.put(Constant.RETURN_CODE, Constant.CronJobStatus.ERROR);
                result.put(Constant.RETURN_DESC, "Exception occurs while doing service["
                        + jobName + "][" + callType + "][" + DateUtil.format(runTime) + "], " + e);
            }
            return null;
        } finally {
            if (isUseDB) {
                cronJobJdbc.updateLastRunTime(conn, jobName, runTime);
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        Constant.LOG_CRON.error("Exception when close connection:" + e);
                    }
                }
            }
        }
    }
}
