package com.andycugb.cron;

/**
 * Created by jbcheng on 2016-04-08.
 */
public interface Inner {
    String getBeanId();

    String getClassName();

    String getMethodDesc();

    String getMethodName();

    String getReturnType();

    String[] getVariableTypes();

    String[] getVariableValues();

    String toString();
}
