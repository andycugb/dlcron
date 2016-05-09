package com.andycugb.cron.db;

import com.andycugb.cron.CronModelException;
import com.andycugb.cron.ExecMethodDesc;
import com.andycugb.cron.util.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by jbcheng on 2016-03-17.
 */
public class CronJobModel {
    private String id;
    private String cronName;
    private String serviceName;
    private String cronExpression;
    private String limitIp;
    private boolean isBlock;
    private boolean isFirstIp;
    private String cronDesc;
    private int fireOnStartUp;
    private String group;
    private Timestamp lastRunTime;
    private Set<String> runOnIps;
    private ExecMethodDesc execMethod;
    private AtomicBoolean parsed = new AtomicBoolean(false);


    public CronJobModel() {

    }

    /**
     * create instance of CronJobModel.
     * 
     * @param cronModel init instance
     */
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

    public ExecMethodDesc getExecMethod() {
        return execMethod;
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

    public Timestamp getLastRunTime() {
        return lastRunTime;
    }

    public void setLastRunTime(Timestamp lastRunTime) {
        this.lastRunTime = lastRunTime;
    }

    /**
     * set run type by given limit ips.
     * 
     * @param serverIp local ip must be given
     * @return run type
     */
    public int getRunType(String serverIp) {
        int runType;
        if (StringUtils.isBlank(serverIp) || StringUtils.isBlank(this.limitIp)) {
            runType = Constant.RunType.RUN_ON_NONE;
        } else {
            String realLimitIp = this.limitIp;
            if (this.limitIp.toLowerCase().startsWith("block:")) {
                realLimitIp = this.limitIp.substring("block:".length());
            }
            if (CollectionUtils.isEmpty(runOnIps)) {
                runOnIps = new HashSet<String>();
                String[] ips = realLimitIp.split(";");
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
                    runType = Constant.RunType.RUN_ON_ANY;
                } else {
                    runType = Constant.RunType.RUN_ON_OTHER;
                }
            }
        }
        return runType;
    }

    /**
     * parse limit expression.
     */
    public synchronized void parse() {
        if (!this.parsed.get()) {
            this.check();
            String realLimitIp = this.limitIp;
            if (this.limitIp.toLowerCase().startsWith("block:")) {
                realLimitIp = this.limitIp.substring("block:".length());
                this.isBlock = true;
            }

            String firstIp = realLimitIp.split(";")[0];
            if (firstIp.equals(Constant.SERVER_IP)) {
                this.isFirstIp = true;
            }

            String[] services = this.serviceName.split(";");
            int serviceLength = services.length;
            if (serviceLength > 1) {
                throw new CronModelException("Too many services in service name, "
                        + this.toString());
            }
            // String beanName.methodName(String paramName...)
            int lIndex = services[serviceLength].indexOf("(");
            int rIndex = services[serviceLength].indexOf(")");
            if (lIndex == -1 || rIndex == -1 || rIndex < lIndex) {
                throw new CronModelException("Incorrect bracket in service name, "
                        + this.toString());
            }

            String servicePath = services[serviceLength].substring(0, lIndex);
            String[] clazzMethod = servicePath.split(" ");
            String returnType;
            String methodPath;
            if (clazzMethod.length == 1) { // default return type
                returnType = "void";
                methodPath = clazzMethod[0].trim();
            } else {
                if (clazzMethod.length != 2) {
                    throw new CronModelException("Too many spaces in service name, "
                            + this.toString());
                }
                returnType = clazzMethod[0].trim();
                methodPath = clazzMethod[1].trim();
            }

            int pointIndex = methodPath.lastIndexOf(".");
            if (pointIndex == -1) {
                throw new CronModelException(
                        "Missing point(.) between bean and method in service name, "
                                + this.toString());
            }
            if (pointIndex == methodPath.length() - 1) {
                throw new CronModelException("Missing method name in service name, "
                        + this.toString());
            }

            String beanName = methodPath.substring(0, pointIndex - 1);
            String methodNameStr = methodPath.substring(pointIndex + 1);

            String[] paramTypes;
            String[] paramValues;
            if (lIndex + 1 < rIndex) {
                String paramsStr = services[serviceLength].substring(lIndex + 1, rIndex);
                String[] params = paramsStr.split(",");
                int length = params.length;
                paramTypes = new String[length];
                paramValues = new String[length];

                for (int j = 0; j < length; j++) {
                    String param = params[j].trim();
                    int split = param.indexOf(" ");
                    if (split == -1) {
                        throw new CronModelException("Incorrect variable in service name, "
                                + this.toString());
                    }
                    paramTypes[j] = param.substring(0, split);
                    paramValues[j] = param.substring(split + 1).replaceAll("\"", "").trim();
                }
            } else {
                paramTypes = paramValues = new String[0];
            }

            this.execMethod =
                    new ExecMethod(beanName, methodNameStr, paramValues, paramTypes, returnType);
            this.parsed.set(true);
        }
    }

    private void check() {
        if (StringUtils.isBlank(this.cronName) || StringUtils.isBlank(this.serviceName)
                || StringUtils.isBlank(this.cronExpression) || StringUtils.isBlank(this.limitIp)) {
            throw new CronModelException("Imperfect cron model, " + this.toString());
        } else if (this.limitIp.toLowerCase().contains("block:")
                && !this.limitIp.startsWith("block:")) {
            throw new CronModelException("Error limitIp, " + this.toString());
        } else if (this.limitIp.toLowerCase().startsWith("block:")
                && StringUtils.isBlank(this.limitIp.substring("block:".length()))) {
            throw new CronModelException("Error limitIp, " + this.toString());
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("cronName=").append(this.cronName);
        builder.append(", serviceName=").append(this.serviceName);
        builder.append(", cronExpression=").append(this.cronExpression);
        builder.append(", limitIp=").append(this.limitIp);
        return builder.toString();
    }

    private class ExecMethod implements ExecMethodDesc {
        private String beanId;
        private String methodName;
        private String[] variableValues;
        private String[] variableTypes;
        private String returnType;

        ExecMethod(String beanId, String methodName, String[] variableValues,
                String[] variableTypes, String returnType) {
            this.beanId = beanId;
            this.methodName = methodName;
            this.variableValues = variableValues;
            this.variableTypes = variableTypes;
            this.returnType = returnType;
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
            StringBuilder builder = new StringBuilder();
            builder.append("beanId=").append(this.beanId);
            builder.append(", methodName=").append(this.methodName);
            builder.append(", variableValues=").append(Arrays.toString(this.variableValues));
            builder.append(", variableTypes=").append(Arrays.toString(this.variableTypes));
            builder.append(", returnType=").append(this.returnType);
            return builder.toString();
        }
    }
}
