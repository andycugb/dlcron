package com.andycugb.cron;

/**
 * Created by jbcheng on 2016-04-08.
 */
public class CronModelException extends RuntimeException {
    private static final long serialVersionUID = 5348312283301889114L;

    public CronModelException() {
    }

    public CronModelException(String message, Throwable cause) {
        super(message, cause);
    }

    public CronModelException(String message) {
        super(message);
    }

    public CronModelException(Throwable cause) {
        super(cause);
    }
}
