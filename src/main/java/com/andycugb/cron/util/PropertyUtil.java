package com.andycugb.cron.util;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by jbcheng on 2016-03-16.
 */
public class PropertyUtil {

    private static Properties properties = null;

    static {
        ClassLoader loader = PropertyUtil.class.getClassLoader();
        if (loader == null) {
            String msg = "error when get class load";
            Constant.LOG_CRON.error(msg);
            throw new RuntimeException(msg);
        }
        InputStream stream = loader.getResourceAsStream("config.properties");
        Constant.LOG_CRON.debug("[initProp] init InputStream by cron.properties.");
        if (stream == null) {
            stream = loader.getResourceAsStream("/config.properties");
            Constant.LOG_CRON.debug("[initProp] init InputStream by /cron.properties.");
        }
        if (stream == null) {
            stream = loader.getResourceAsStream("com/andycugb/cron/config.properties");
            Constant.LOG_CRON
                    .debug("[initProp] init InputStream by com/andycugb/cron/cron.properties.");
        }
        if (stream != null) {
            try {
                properties.load(stream);
            } catch (IOException e) {
                Constant.LOG_CRON.error("[initProp] error when load resources");
                throw new RuntimeException(e);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    Constant.LOG_CRON.error("[initProp] error when close resources");
                    throw new RuntimeException(e);
                }
            }
        }
    }


    public static String getStringProperty(String key, String def) {
        String value = getStringProperty(key);
        if (StringUtils.isBlank(value)) {
            value = def;
        }
        return value;
    }

    public static String getStringProperty(String key) {
        String value = properties.getProperty(key);
        if (StringUtils.isBlank(value)) {
            Constant.LOG_CRON.warn("cannot find value by key:" + key);
        }
        return value;
    }

    public static int getIntProperty(String key, int def) {
        try {
            return getIntProperty(key);
        } catch (NumberFormatException e) {
            Constant.LOG_CRON.warn("wrong number to parse:" + e);
        }
        return def;
    }

    public static int getIntProperty(String key) throws NumberFormatException {
        String value = properties.getProperty(key);
        if (StringUtils.isNumeric(value)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Constant.LOG_CRON.warn("wrong number to parse:" + value + "--" + e);
                throw new NumberFormatException(e.getMessage());
            }
        } else {
            throw new NumberFormatException("wrong number to parse:" + value);
        }
    }
}
