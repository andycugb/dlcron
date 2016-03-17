package com.andycugb.cron.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jbcheng on 2016-03-17.
 */
public class DateUtil {
    private static final String TIME_FORMAT="yyyy-MM-dd HH:mm:ss";
    public static String formatDate(Date date){
        return date==null?"":new SimpleDateFormat(TIME_FORMAT).format(date);
    }
}
