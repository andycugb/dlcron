package com.andycugb.cron;

import com.andycugb.cron.db.CronJobDao;
import com.andycugb.cron.db.CronJobModel;
import com.andycugb.cron.db.QuartzManager;
import com.andycugb.cron.util.Constant;
import com.andycugb.cron.util.IpUtil;
import com.andycugb.cron.util.PropertyUtil;
import com.andycugb.cron.zk.ZooKeeperConfig;
import com.andycugb.cron.zk.ZooKeeperSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by jbcheng on 2016-03-19.
 */
public class StartUpListener implements ApplicationContextAware, ApplicationListener {

    @Autowired
    private QuartzManager quartzManager;
    @Autowired
    private CronJobDao cronJobDao;
    private AtomicBoolean isRun = new AtomicBoolean(true);

    @PostConstruct
    public void init() {
        Constant.LOG_CRON.info("[StartUp]Start to init cron...");
        try {
            this.initServerIp();
            Constant.LOG_CRON.info("[StartUp]Server ip is " + Constant.SERVER_IP);
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
        if (isRun.getAndSet(false)) {
            Thread start = new Thread(new StartUpListener.InnerStartUpThread());
            start.start();
        }
    }

    class InnerStartUpThread implements Runnable {
        public void run() {
            StartUpListener.this.refreshCron();
        }
    }

    private synchronized void refreshCron() {
        Constant.LOG_CRON.info("[Refresh]start to refresh cron config");
        List<CronJobModel> dbCronList = this.loadFormDB();
        if (CollectionUtils.isNotEmpty(dbCronList)) {
            Map<String, CronJobModel> tempCronMap = new HashMap<String, CronJobModel>();
            Map<String, CronJobModel> deletedCronMap = new HashMap<String, CronJobModel>();
            Iterator<CronJobModel> cancelCronMap = dbCronList.iterator();
            CronJobModel oldCron;
            while (cancelCronMap.hasNext()) {
                CronJobModel addCron = cancelCronMap.next();

                try {
                    addCron.parse();
                    String[] crons = addCron.getCronExpression().split(";");
                    int len = crons.length;
                    for (int index = 0; index < len; index++) {
                        String cron1 =
                                (index == 0) ? addCron.getCronName() : addCron.getCronName()
                                        + "_" + index;
                        oldCron = new CronJobModel(addCron);
                        oldCron.setCronExpression(crons[index]);
                        oldCron.setCronName(cron1);
                        if (deletedCronMap.containsKey(cron1)) {
                            deletedCronMap.put(cron1,oldCron);
                        } else if (tempCronMap.containsKey(cron1)) {
                            tempCronMap.remove(cron1);
                            deletedCronMap.put(cron1,oldCron);
                        } else {
                            tempCronMap.put(cron1, oldCron);
                        }
                    }
                } catch (Exception e) {
                    Constant.LOG_CRON.error("Unknown exception occurs while register cron, " + addCron.toString(), e);
                }
            }
        }

    }

    private List<CronJobModel> loadFormDB() {
        return cronJobDao.getAllCronByGroup(PropertyUtil.getStringProperty("cron.group.name"));
    }

    private void initServerIp() {
        if (StringUtils.isBlank(Constant.SERVER_IP)) {
            Constant.SERVER_IP = this.loadServerIp();
        }
    }

    private String loadServerIp() {
        try {
            List<String> ipList = IpUtil.getServerIps(false);
            if (CollectionUtils.isNotEmpty(ipList)) {
                return ipList.get(0);
            }
        } catch (Exception e) {
            Constant.LOG_CRON.fatal("get serverIp error" + e);
        }
        return null;
    }

    private void initZkConfig() {
        ZooKeeperConfig zkConfig = ZooKeeperConfig.getInstance();
        zkConfig.setConnectUrl(PropertyUtil.getStringProperty("cron.zookeeper.connect.url"));
        zkConfig.setProduct(PropertyUtil.getStringProperty("cron.zookeeper.product.name"));
        zkConfig.setRoot(PropertyUtil.getStringProperty("cron.zookeeper.root.name"));
        zkConfig.setTimeout(PropertyUtil.getIntProperty("cron.zookeeper.connect.timeout", 3000));
        Constant.LOG_CRON.debug("[initZkConfig] Set param. [connectUrl = "
                + zkConfig.getConnectUrl() + "][product = " + zkConfig.getProduct() + "][root = "
                + zkConfig.getRoot() + "][timeOut = " + zkConfig.getTimeout() + "].");
        if (zkConfig.isUseZK()) {

            try {
                ZooKeeperSupport.setZookeeper(ZooKeeperSupport.createNewZooKeeper());
            } catch (Exception e) {
                Constant.LOG_CRON.error("[initZkConfig] error happen where new zookeeper:" + e);
            }
        }
    }
}
