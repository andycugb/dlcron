package com.andycugb.cron.db;

import com.andycugb.cron.util.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jbcheng on 2016-03-17.
 */
public class CronJobModel {
    private String id;
    private String cronName;
    private String serviceName;
    private String cronExpression;
    private String limitIp;
    private Set<String> limitIpSet;
    private boolean isBlock;
    private boolean isFirstIp;
    private String cronDesc;
    private int fireOnStartUp;
    private String group;
    private String enhancedSubClassSuffix;
    private Timestamp lastRunTime;
    private Set<String> runOnIps;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCronName() {
        return cronName;
    }

    public void setCronName(String cronName) {
        this.cronName = cronName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getLimitIp() {
        return limitIp;
    }

    public void setLimitIp(String limitIp) {
        this.limitIp = limitIp;
    }

    public Set<String> getLimitIpSet() {
        return limitIpSet;
    }

    public void setLimitIpSet(Set<String> limitIpSet) {
        this.limitIpSet = limitIpSet;
    }

    public boolean isBlock() {
        return isBlock;
    }

    public void setIsBlock(boolean isBlock) {
        this.isBlock = isBlock;
    }

    public boolean isFirstIp() {
        return isFirstIp;
    }

    public void setIsFirstIp(boolean isFirstIp) {
        this.isFirstIp = isFirstIp;
    }

    public String getCronDesc() {
        return cronDesc;
    }

    public void setCronDesc(String cronDesc) {
        this.cronDesc = cronDesc;
    }

    public int getFireOnStartUp() {
        return fireOnStartUp;
    }

    public void setFireOnStartUp(int fireOnStartUp) {
        this.fireOnStartUp = fireOnStartUp;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getEnhancedSubClassSuffix() {
        return enhancedSubClassSuffix;
    }

    public void setEnhancedSubClassSuffix(String enhancedSubClassSuffix) {
        this.enhancedSubClassSuffix = enhancedSubClassSuffix;
    }

    public Timestamp getLastRunTime() {
        return lastRunTime;
    }

    public void setLastRunTime(Timestamp lastRunTime) {
        this.lastRunTime = lastRunTime;
    }

    /**
     * set run type by given limit ips
     * 
     * @param serverIp local ip must be given
     * @return run type
     */
    public int getRunType(String serverIp) {
        int runType;
        if (StringUtils.isBlank(serverIp) || StringUtils.isBlank(this.limitIp)) {
            runType = Constant.RunType.RUN_ON_NONE;
        } else {
            if (CollectionUtils.isEmpty(runOnIps)) {
                runOnIps = new HashSet<String>();
                String[] ips = limitIp.split(";");
                for (String ip : ips) {
                    runOnIps.add(ip);
                }
            }
            if (this.runOnIps.contains("1.1.1.1")) {
                runType = Constant.RunType.RUN_ON_NONE;
            } else {
                if (this.runOnIps.size() == 1 && this.runOnIps.contains("0.0.0.0")) {
                    runType = Constant.RunType.RUN_ON_ALL;
                } else if (this.runOnIps.contains(serverIp)) {
                    runType = Constant.RunType.RUN_ON_LOCAL;
                } else {
                    runType = Constant.RunType.RUN_ON_OTHER;
                }
            }
        }

        return runType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cronName=").append(this.cronName);
        sb.append(", serviceName=").append(this.serviceName);
        sb.append(", cronExpression=").append(this.cronExpression);
        sb.append(", limitIp=").append(this.limitIp);
        return sb.toString();
    }
}
