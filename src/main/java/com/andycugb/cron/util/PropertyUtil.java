package com.andycugb.cron.util;

import org.apache.commons.lang.StringUtils;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created by jbcheng on 2016-03-16.
 */
public class PropertyUtil {

    private ResourceBundle bundle = null;

    /**
     * create instance by given parameter.
     * @param baseName resource file name
     * @param locale language local
     */
    public PropertyUtil(String baseName, Locale locale) {
        if (locale == null) {
            locale = Locale.CHINA;
        }
        Constant.LOG_CRON.debug("[initProp] init InputStream by," + baseName);
        try {
            bundle = ResourceBundle.getBundle(baseName,locale);
        } catch (NullPointerException e) {
            Constant.LOG_CRON.error("[initProp] error when load resources:" + baseName + "," + e);
        } catch (MissingResourceException e) {
            Constant.LOG_CRON.error("[initProp] missing resources when load:" + baseName + ","
                    + e);
        }
    }

    /**
     * get String value by given key,when failed,use def instead.
      * @param key property key
     * @param def default value
     * @return  String value
     */
    public String getStringProperty(String key, String def) {
        String value = getStringProperty(key);
        if (StringUtils.isBlank(value)) {
            value = def;
        }
        return value;
    }

    /**
     * get String value by given key.
     * @param key property key
     * @return String value
     */
    public String getStringProperty(String key) {
        String value = bundle.getString(key);
        if (StringUtils.isBlank(value)) {
            Constant.LOG_CRON.warn("cannot find value by key:" + key);
        }
        return value;
    }

    /**
     * get int value by given key,when parse fail use def instead.
     * @param key property key
     * @param def default value
     * @return int value
     */
    public int getIntProperty(String key, int def) {
        try {
            return getIntProperty(key);
        } catch (NumberFormatException e) {
            Constant.LOG_CRON.warn("wrong number to parse:" + e);
        }
        return def;
    }

    /**
     * get int value by given key.
     * @param key property key
     * @return int value
     * @throws NumberFormatException when int parse fail
     */
    public int getIntProperty(String key) throws NumberFormatException {
        String value = bundle.getString(key);
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
