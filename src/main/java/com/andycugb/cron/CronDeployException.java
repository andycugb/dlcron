package com.andycugb.cron;

public class CronDeployException extends RuntimeException {
    private static final long serialVersionUID = 5348312283301889114L;

    public CronDeployException() {}

    public CronDeployException(String message, Throwable cause) {
        super(message, cause);
    }

    public CronDeployException(String message) {
        super(message);
    }

    public CronDeployException(Throwable cause) {
        super(cause);
    }
}
