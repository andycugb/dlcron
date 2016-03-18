package com.andycugb.cron.util;

import org.apache.log4j.Logger;

/**
 * Created by jbcheng on 2016-03-16.
 */
public final class Constant {
    public static String SERVER_IP;
    public static final class CronJobStatus{
        public static final int SUCCESS=200;
        public static final int ERROR=500;
        public static final int RETRY=301;
    }

    public static final class RunType {
        public static final int RUN_ON_ALL = 1;
        public static final int RUN_ON_NONE = 2;
        public static final int RUN_ON_OTHER = 3;
        public static final int RUN_ON_LOCAL = 4;
    }

    public final static Logger LOG_CRON = Logger.getLogger("cron.log");
}
