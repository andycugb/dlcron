package com.andycugb.cron.model;

import java.util.Date;

/**
 * Created by jbcheng on 2016-04-27.
 */
public class Cron {
    private String id;
    private String cronName;// 任务名称
    private String serviceName;// 服务名称 返回值类型 bean_id.方法(参数1类型 参数1,参数2类型 参数2,参数3类型 参数3);
    private String cronExpression;// cron表达式
    private String limitIp;// 限制ip
    private String cronDesc;// cron描述
    private int fireOnStartUp;// 启动时是否执行；1-执行；0-不执行
    private String enhancedSubClassSuffix;// 增强子类的后缀
    private Date lastFireTime;// 上次执行时间
    private String groupName;// 所属系统

    private String execMachines;
    private String execMachineConfirm;


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

    public String getEnhancedSubClassSuffix() {
        return enhancedSubClassSuffix;
    }

    public void setEnhancedSubClassSuffix(String enhancedSubClassSuffix) {
        this.enhancedSubClassSuffix = enhancedSubClassSuffix;
    }

    public Date getLastFireTime() {
        return lastFireTime;
    }

    public void setLastFireTime(Date lastFireTime) {
        this.lastFireTime = lastFireTime;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getExecMachines() {
        return execMachines;
    }

    public void setExecMachines(String execMachines) {
        this.execMachines = execMachines;
    }

    public String getExecMachineConfirm() {
        return execMachineConfirm;
    }

    public void setExecMachineConfirm(String execMachineConfirm) {
        this.execMachineConfirm = execMachineConfirm;
    }
}
