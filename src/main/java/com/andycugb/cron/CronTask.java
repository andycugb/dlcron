package com.andycugb.cron;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Created by jbcheng on 2016-03-17.
 */
public interface CronTask {

    /**
     * do schedule business logic.
     * 
     * @param jobName invoke method
     * @param callType invoke type
     * @param runTime execute time
     * @param isUseDB whether use db config
     * @return exec status map
     */
    Map<String, Object> doTask(String jobName, String callType, Timestamp runTime, boolean isUseDB);
}
