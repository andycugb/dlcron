package com.andycugb.cron.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jbcheng on 2016-03-17.
 */
public class DateUtil {
    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_FORMAT_1 = "yyyyMMdd-HHmmss";

    public static String format(Date date) {
        return date == null ? "" : new SimpleDateFormat(TIME_FORMAT).format(date);
    }

    /**
     * get interval seconds of two given timestamp.
     * 
     * @param first first timestamp
     * @param second second timestamp
     * @return interval seconds
     */
    public static long getInterValSeconds(Timestamp first, Timestamp second) {
        if (first == null || second == null) {
            return 0L;
        }
        long interval = first.getTime() - second.getTime();
        return Math.abs(interval / 1000);

    }

    public static String getExecTime(Timestamp timestamp) {
        return new SimpleDateFormat(TIME_FORMAT_1).format(timestamp);
    }

    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static Timestamp toTimeStamp(Date date) {
        return new Timestamp(date.getTime());
    }
}
