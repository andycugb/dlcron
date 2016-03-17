package com.andycugb.cron.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by jbcheng on 2016-03-16.
 */
public class PropertyUtil {

    private ResourceBundle bundle = null;
    private final static String DEFAULT = "config";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PropertyUtil(String baseName, Locale locale) {
        String prefix = DEFAULT;
        if (StringUtils.isNotBlank(baseName)) {
            prefix = baseName.trim();
        }
        if (locale == null) {
            locale = Locale.CHINA;
        }
        try {
            bundle = ResourceBundle.getBundle(prefix, locale);
        } catch (Exception e) {
            logger.error("error when load property file:" + e);
        }
    }

    public String getProperty(String key) {
        String value = bundle.getString(key);
        if (StringUtils.isBlank(value)) {
            logger.error("cannot find value by key:" + key);
        }
        return value;
    }
}
