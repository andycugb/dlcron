package com.andycugb.cron.util;

/**
 * Created by jbcheng on 2016-03-18.
 */
public class ClassGenerator {

    public static ClassGenerator getInstance() {
        return ClassHolder.INSTANCE;
    }

    public Class<?> getClazz(String methodName, String clazzName) {
        return null;
    }

    static class ClassHolder {
        private static ClassGenerator INSTANCE = new ClassGenerator();
    }
}
