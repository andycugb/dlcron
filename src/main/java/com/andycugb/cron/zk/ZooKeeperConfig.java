package com.andycugb.cron.zk;

import org.apache.commons.lang.StringUtils;

/**
 * Created by jbcheng on 2016-03-16.
 */
public class ZooKeeperConfig {
    // zk path "\root\productName\jobName\locks_server_ip"
    private String root;
    private String product;
    private String connectUrl;
    private int timeout = 30000;// default timeout

    public static ZooKeeperConfig getInstance() {
        return ZKConfigHolder.INSTANCE;
    }

    public boolean isUseZK() {
        return ZooKeeperSupport.isUseZK ? this.checkConfig() : false;
    }

    // check path
    private boolean checkConfig() {
        boolean flag = false;
        if (StringUtils.isBlank(this.root) || !this.root.startsWith("/")
                || this.root.endsWith("/")) {
            return flag;
        } else if (StringUtils.isBlank(this.product) || !this.product.startsWith("/")
                || this.product.endsWith("/")) {
            return flag;
        } else if (StringUtils.isBlank(this.connectUrl)) {
            return flag;
        } else {
            flag = true;
            return flag;
        }
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getConnectUrl() {
        return connectUrl;
    }

    public void setConnectUrl(String connectUrl) {
        this.connectUrl = connectUrl;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    // inner class as singleton
    static class ZKConfigHolder {
        private static final ZooKeeperConfig INSTANCE = new ZooKeeperConfig();
    }
}
