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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

/**
 * Created by jbcheng on 2016-03-19.
 */
@Service
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
        Constant.APP_CONTEXT = applicationContext;
    }

    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (isRun.getAndSet(false)) {
            Thread start = new Thread(new StartUpListener.InnerStartUpThread());
            start.start();
        }
    }

    public synchronized void refreshCron() {
        Constant.LOG_CRON.info("[Refresh]start to refresh cron config");
        List<CronJobModel> dbCronList = this.loadFormDB();
        if (CollectionUtils.isNotEmpty(dbCronList)) {
            Map<String, CronJobModel> tempCronMap = new HashMap<String, CronJobModel>();
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
                        tempCronMap.put(cron1, oldCron);
                    }
                } catch (Exception e) {
                    Constant.LOG_CRON.error("Unknown exception occurs while register cron, "
                            + addCron.toString(), e);
                }
            }

            // add、cancel job
            ArrayList<CronJobModel> addList = new ArrayList<CronJobModel>();
            ArrayList<CronJobModel> cancelList = new ArrayList<CronJobModel>();
            Iterator<CronJobModel> iterator = tempCronMap.values().iterator();
            while (iterator.hasNext()) {
                CronJobModel model = iterator.next();
                oldCron = this.quartzManager.getJobByName(model.getCronName());
                if (null == oldCron) {
                    if (this.isExecute(model)) {
                        addList.add(model);
                    }
                } else if (!oldCron.equals(model)) {
                    if (this.isExecute(oldCron)) {
                        cancelList.add(oldCron);
                    }
                    if (this.isExecute(model)) {
                        addList.add(model);
                    }
                }
            }
            iterator = this.quartzManager.geAllCronModels().values().iterator();
            while (iterator.hasNext()) {
                oldCron = iterator.next();
                if (!tempCronMap.containsKey(oldCron.getCronName())) {
                    cancelList.add(oldCron);
                }
            }
            this.quartzManager.reloadCronModels(tempCronMap);
            int cancelSize = cancelList.size(), addSize = addList.size();
            if (cancelSize > 0) {
                this.cancelJob(cancelList);
            }
            if (addSize > 0) {
                this.addJob(addList);
            }
            Constant.LOG_CRON.info("[Refresh]Finish to refresh cron config, cancel " + cancelSize
                    + " old crons, add " + addSize + " new crons");
        }

    }

    private void cancelJob(ArrayList<CronJobModel> cancelList) {
        Iterator<CronJobModel> iterator = cancelList.iterator();
        while (iterator.hasNext()) {
            CronJobModel cron = iterator.next();
            this.quartzManager.deleteJob(cron.getCronName());
            Constant.LOG_CRON.debug("[cancelJob] cancel a Job , [" + cron + "]");
        }
    }

    private void addJob(ArrayList<CronJobModel> addList) {
        Iterator<CronJobModel> iterator = addList.iterator();
        while (iterator.hasNext()) {
            CronJobModel cron = iterator.next();
            this.quartzManager.addNewJob(cron);
            Constant.LOG_CRON.debug("[cancelJob] cancel a Job , [" + cron + "]");
        }

    }

    private boolean isExecute(CronJobModel model) {
        return ZooKeeperConfig.getInstance().isUseZK() ? this.isExecuteForZK(model)
                : isExecuteSingle(model);
    }

    private boolean isExecuteForZK(CronJobModel model) {
        if (null == model) {
            return false;
        } else {
            int runType = model.getRunType(Constant.SERVER_IP);
            return (runType != Constant.RunType.RUN_ON_NONE)
                    && (runType != Constant.RunType.RUN_ON_OTHER || !model.getIsBlock());
        }
    }

    private boolean isExecuteSingle(CronJobModel model) {
        if (null == model) {
            return false;
        } else {
            int runType = model.getRunType(Constant.SERVER_IP);
            return runType == Constant.RunType.RUN_ON_ALL || model.getIsBlock()
                    && model.getIsFirstIp() || !model.getIsBlock()
                    && runType == Constant.RunType.RUN_ON_ANY;
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

    class InnerStartUpThread implements Runnable {
        public void run() {
            StartUpListener.this.refreshCron();
        }
    }
}
