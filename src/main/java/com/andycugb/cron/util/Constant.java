package com.andycugb.cron.util;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * Created by jbcheng on 2016-03-16.
 */
public final class Constant {
    public static final String CRON_SINGLE_CHECK = "cron.single.check";
    public static final Logger LOG_CRON = Logger.getLogger("cron.log");
    public static final String RETURN_DESC = "desc";
    public static final String RETURN_CODE = "code";
    public static ApplicationContext APP_CONTEXT;
    public static String SERVER_IP;

    public static final class CronJobStatus {
        public static final int SUCCESS = 200;
        public static final int ERROR = 500;
        public static final int RETRY = 301;// zk connect exception
    }

    public static final class RunType {
        public static final int RUN_ON_ALL = 1;// all server run
        public static final int RUN_ON_NONE = 2;// none server run
        public static final int RUN_ON_OTHER = 3;// any server run exclude local
        public static final int RUN_ON_ANY = 4;// any server run include local
    }
}
