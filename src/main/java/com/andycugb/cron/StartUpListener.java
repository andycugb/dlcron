package com.andycugb.cron;

import com.andycugb.cron.db.QuartzManager;
import com.andycugb.cron.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.annotation.PostConstruct;

/**
 * Created by jbcheng on 2016-03-19.
 */
public class StartUpListener implements ApplicationContextAware, ApplicationListener {

    @Autowired
    private QuartzManager quartzManager;

    @PostConstruct
    public void init() {
        Constant.LOG_CRON.info("[StartUp]Start to init cron...");
        try {
            this.initServerIp();
            Constant.LOG_CRON.info("[StartUp]Server ip is " + Constant.SERVER_IP);
            this.initProps();
            Constant.LOG_CRON.debug("[StartUp]initProp has been done.");
            this.initZkConfig();
            Constant.LOG_CRON.debug("[StartUp]initZkConfig has been done.");
            this.quartzManager.startScheduler();
            Constant.LOG_CRON.debug("[StartUp]startScheduler has been done.");
        } catch (Exception e) {
            Constant.LOG_CRON.error("[StartUp]Fail to start cron system " + e);
        }

        Constant.LOG_CRON.info("[StartUp]Finish to init cron...");
    }

    public void setApplicationContext(ApplicationContext applicationContext) {

    }

    public void onApplicationEvent(ApplicationEvent applicationEvent) {

    }

    private void initServerIp() {

    }

    private void initProps() {

    }

    private void initZkConfig() {
        if (Constant.PROP_UTIL==null){
            this.initProps();
        }
        
    }
}
