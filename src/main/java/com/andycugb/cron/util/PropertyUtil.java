package com.andycugb.cron.util;

import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by jbcheng on 2016-03-16.
 */
public class PropertyUtil {

    private static final String BASE_NAME = "cron.properties";
    private static Properties properties = System.getProperties();

    static {
        InputStream stream = null;
        String baseName = BASE_NAME;
        try {
            ClassLoader loader = PropertyUtil.class.getClassLoader();
            Constant.LOG_CRON.debug("[initProp] init InputStream by resource:" + baseName);
            stream = loader.getResourceAsStream(baseName);
            if (stream == null) {
                baseName = "/" + baseName;
                Constant.LOG_CRON.debug("[initProp] init InputStream by resources:" + baseName);
                stream = loader.getResourceAsStream(baseName);
            }
            if (stream == null) {
                baseName = "com/andycugb/cron/" + baseName;
                Constant.LOG_CRON.debug("[initProp] init InputStream by resources:" + baseName);
                stream = loader.getResourceAsStream(baseName);
            }
            if (stream != null) {
                try {
                    properties.load(stream);
                } catch (Exception e) {
                    Constant.LOG_CRON.error("error when load resource:" + e);
                }
            }
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    Constant.LOG_CRON.error("error when close stream resource:" + e);
                }
            }
            if (properties == null) {
                throw new RuntimeException("[initPro] Can not find zk config resource");
            }
        }
    }

    /**
     * get String value by given key.
     * 
     * @param key property key
     * @return String value
     */
    public static String getStringProperty(String key) {
        String value = properties.getProperty(key);
        if (StringUtils.isBlank(value)) {
            Constant.LOG_CRON.warn("cannot find value by key:" + key);
        }
        return value;
    }

    /**
     * get int value by given key,when parse fail use def instead.
     * 
     * @param key property key
     * @param def default value
     * @return int value
     */
    public static int getIntProperty(String key, int def) {
        try {
            return getIntProperty(key);
        } catch (NumberFormatException e) {
            Constant.LOG_CRON.warn("wrong number to parse:" + e);
        }
        return def;
    }

    /**
     * get int value by given key.
     * 
     * @param key property key
     * @return int value
     * @throws NumberFormatException when int parse fail
     */
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

    public static void main(String[] args) {
        Enumeration property = properties.propertyNames();
        Object key;
        while (property.hasMoreElements()) {
            key = property.nextElement();
            System.out.println("key:" + key + ",value:" + properties.get(key));
        }
    }
}
