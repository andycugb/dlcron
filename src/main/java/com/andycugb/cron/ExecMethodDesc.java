package com.andycugb.cron;

/**
 * Created by jbcheng on 2016-04-08.
 */
public interface ExecMethodDesc {
    String getBeanId();

    String getMethodName();

    String getReturnType();

    String[] getVariableTypes();

    String[] getVariableValues();

    String toString();
}
