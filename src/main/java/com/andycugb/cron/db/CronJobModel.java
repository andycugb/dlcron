package com.andycugb.cron.db;

import com.andycugb.cron.Inner;
import com.andycugb.cron.util.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.Arrays;
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

    public boolean getIsBlock() {
        return isBlock;
    }

    public void setIsBlock(boolean isBlock) {
        this.isBlock = isBlock;
    }

    public boolean getIsFirstIp() {
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

    public CronJobModel(){

    }

    public CronJobModel(CronJobModel cronModel) {
        this.cronName = cronModel.getCronName();
        this.serviceName = cronModel.getServiceName();
        this.cronExpression = cronModel.getCronExpression();
        this.limitIp = cronModel.getLimitIp();
        this.cronDesc = cronModel.getCronDesc();
        this.fireOnStartUp = cronModel.getFireOnStartUp();
        this.group = cronModel.getGroup();
        this.isBlock = cronModel.getIsBlock();
        this.isFirstIp = cronModel.getIsFirstIp();
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

    public synchronized void parse(){
        /*if (!this.parsed.get()) {
            this.check();
            String realLimitIp=this.limitIp;
            if (this.limitIp.toLowerCase().startsWith("block:")) {
                realLimitIp=this.limitIp.substring("block:".length());
                this.isBlock=true;
            }

            String firstIp=realLimitIp.split(";")[0];
            if (firstIp.equals(Constant.SERVER_IP)){
                this.isFirstIp=true;
            }

            String[] services=this.serviceName.split(";");
            int serviceLength=services.length;
            ArrayList<Entity> innerListTmp=new ArrayList<Entity>();

            for (int i=0;i<serviceLength;++i){
                int lIndex=services[i].indexOf("(");
                int rIndex=services[i].indexOf(")");
                if (lIndex==-1||rIndex==-1||rIndex<lIndex){
                    throw new CronModelException("Incorrect bracket in service name, " + this.toString());
                }

                String lStr=services[i].substring(0,lIndex);
//                String[] t
            }
        }*/
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

    private class Entity implements Inner {
        private String beanId;
        private String className;
        private String methodName;
        private String[] variableValues;
        private String[] variableTypes;
        private String methodDesc;
        private String returnType;

        Entity(String beanId, String className, String classType, String methodName, String[] variableValues, String[] variableTypes, String methodDesc, String returnType) {
            this.beanId = beanId;
            this.className = className;
            this.methodName = methodName;
            this.variableValues = variableValues;
            this.variableTypes = variableTypes;
            this.methodDesc = methodDesc;
            this.returnType = returnType;
        }

        public String getClassName() {
            return this.className;
        }

        public String getMethodDesc() {
            return this.methodDesc;
        }

        public String getMethodName() {
            return this.methodName;
        }

        public String getReturnType() {
            return this.returnType;
        }

        public String[] getVariableTypes() {
            return this.variableTypes;
        }

        public String[] getVariableValues() {
            return this.variableValues;
        }

        public String getBeanId() {
            return this.beanId;
        }

        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append("beanId=").append(this.beanId);
            b.append(", className=").append(this.className);
            b.append(", methodName=").append(this.methodName);
            b.append(", variableValues=").append(Arrays.toString(this.variableValues));
            b.append(", variableTypes=").append(Arrays.toString(this.variableTypes));
            b.append(", methodDesc=").append(this.methodDesc);
            b.append(", returnType=").append(this.returnType);
            return b.toString();
        }
    }
}
