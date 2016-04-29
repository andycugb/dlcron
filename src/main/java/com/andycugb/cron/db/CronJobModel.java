package com.andycugb.cron.db;

import com.andycugb.cron.CronModelException;
import com.andycugb.cron.Inner;
import com.andycugb.cron.util.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
    private String enhancedSubClassSuffix;
    private Timestamp lastRunTime;
    private Set<String> runOnIps;
    private List<Inner> innerList;
    private AtomicBoolean parsed = new AtomicBoolean(false);


    public CronJobModel() {

    }

    /**
     *  create instance of CronJobModel.
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

    public List<Inner> getInnerList() {
        return innerList;
    }

    public void setInnerList(List<Inner> innerList) {
        this.innerList = innerList;
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

    public String getEnhancedSubClassSuffix() {
        return enhancedSubClassSuffix;
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
            ArrayList<Inner> innerListTmp = new ArrayList<Inner>(serviceLength);

            // String beanName.methodName(String paramName...)
            for (int i = 0; i < serviceLength; ++i) {
                int lIndex = services[i].indexOf("(");
                int rIndex = services[i].indexOf(")");
                if (lIndex == -1 || rIndex == -1 || rIndex < lIndex) {
                    throw new CronModelException("Incorrect bracket in service name, "
                            + this.toString());
                }

                String servicePath = services[i].substring(0, lIndex);
                String[] strArray = servicePath.split(" ");
                String rType;
                String methodPath;
                if (strArray.length == 1) {
                    rType = "void";
                    methodPath = strArray[0].trim();
                } else {
                    if (strArray.length != 2) {
                        throw new CronModelException("Too many spaces in service name, "
                                + this.toString());
                    }
                    rType = strArray[0].trim();
                    methodPath = strArray[1].trim();
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
                String returnType = this.parseReturnType(rType);

                String[] paramTypes;
                String[] paramValues;
                if (lIndex + 1 < rIndex) {
                    String paramsStr = services[i].substring(lIndex + 1, rIndex);
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

                String clazzName =
                        this.parseClazz(beanName, methodNameStr, paramTypes).replaceAll("\"", "")
                                .trim();
                String clazzType = "L" + clazzName + ";";
                String[] parameterTyps = this.parseParam(paramTypes);
                String methodDesc = this.parseMethod(parameterTyps, returnType);
                CronJobModel.Entity entity =
                        new CronJobModel.Entity(beanName, clazzName, clazzType, methodNameStr,
                                paramValues, paramTypes, methodDesc, returnType);

                innerListTmp.add(entity);
            }

            this.combineEnhanceSubClassSuffix();
            this.innerList = innerListTmp;
            this.parsed.set(true);
        }
    }

    private void combineEnhanceSubClassSuffix() {
        this.enhancedSubClassSuffix = String.valueOf(this.serviceName.hashCode());
    }

    private String parseMethod(String[] paramTypes, String returnType) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        int length = paramTypes.length;

        for (int index = 0; index < length; index++) {
            builder.append(paramTypes[index]);
        }
        return builder.append(")").append(returnType).toString();
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

    private String parseClazz(String beanName, String methodName, String[] paramTypes) {
        Object obj = Constant.APP_CONTEXT.getBean(beanName);
        if (null == obj) {
            throw new CronModelException("No such bean id in application context, "
                    + this.toString());
        } else {
            Class clazz = obj.getClass();
            try {
                clazz.getDeclaredMethod(methodName, this.parseParamClazz(paramTypes));
            } catch (NoSuchMethodException e) {
                throw new CronModelException("No such method in bean, " + this.toString(), e);
            }
            return clazz.getName();
        }
    }

    private Class<?>[] parseParamClazz(String[] parameterTypes) {
        if (parameterTypes == null) {
            throw new CronModelException("Parameter types can not be null, " + this.toString());
        } else {
            int length = parameterTypes.length;
            if (length == 0) {
                return new Class[0];
            } else {
                Class[] clazz = new Class[length];

                for (int i = 0; i < length; ++i) {
                    String s0 = parameterTypes[i];
                    if ("boolean".equals(s0)) {
                        clazz[i] = Boolean.TYPE;
                    } else if ("int".equals(s0)) {
                        clazz[i] = Integer.TYPE;
                    } else if ("String".equals(s0)) {
                        clazz[i] = String.class;
                    } else if ("float".equals(s0)) {
                        clazz[i] = Float.TYPE;
                    } else if ("long".equals(s0)) {
                        clazz[i] = Long.TYPE;
                    } else if ("double".equals(s0)) {
                        clazz[i] = Double.TYPE;
                    } else if ("short".equals(s0)) {
                        clazz[i] = Short.TYPE;
                    } else if ("byte".equals(s0)) {
                        clazz[i] = Byte.TYPE;
                    } else if ("char".equals(s0)) {
                        clazz[i] = Character.TYPE;
                    } else if ("Boolean".equals(s0)) {
                        clazz[i] = Boolean.class;
                    } else if ("Integer".equals(s0)) {
                        clazz[i] = Integer.class;
                    } else if ("Float".equals(s0)) {
                        clazz[i] = Float.class;
                    } else if ("Long".equals(s0)) {
                        clazz[i] = Long.class;
                    } else if ("Double".equals(s0)) {
                        clazz[i] = Double.class;
                    } else if ("Short".equals(s0)) {
                        clazz[i] = Short.class;
                    } else if ("Byte".equals(s0)) {
                        clazz[i] = Byte.class;
                    } else {
                        if (!"Character".equals(s0)) {
                            throw new CronModelException("Unknow parameter type, "
                                    + this.toString());
                        }

                        clazz[i] = Character.class;
                    }
                }

                return clazz;
            }
        }
    }

    private String[] parseParam(String[] paramTypes) {
        if (ArrayUtils.isEmpty(paramTypes)) {
            throw new CronModelException("Parameter types can not be null, " + this.toString());
        } else {
            int length = paramTypes.length;
            if (length == 0) {
                return new String[0];
            } else {
                String[] type = new String[length];
                for (int i = 0; i < length; ++i) {
                    String s0 = paramTypes[i].trim();
                    if ("boolean".equals(s0)) {
                        type[i] = "Z";
                    } else if ("int".equals(s0)) {
                        type[i] = "I";
                    } else if ("String".equals(s0)) {
                        type[i] = "Ljava/lang/String;";
                    } else if ("float".equals(s0)) {
                        type[i] = "F";
                    } else if ("long".equals(s0)) {
                        type[i] = "J";
                    } else if ("double".equals(s0)) {
                        type[i] = "D";
                    } else if ("char".equals(s0)) {
                        type[i] = "C";
                    } else if ("byte".equals(s0)) {
                        type[i] = "B";
                    } else if ("short".equals(s0)) {
                        type[i] = "S";
                    } else if ("Integer".equals(s0)) {
                        type[i] = "Ljava/lang/Integer;";
                    } else if ("Float".equals(s0)) {
                        type[i] = "Ljava/lang/Float;";
                    } else if ("Double".equals(s0)) {
                        type[i] = "Ljava/lang/Double;";
                    } else if ("Long".equals(s0)) {
                        type[i] = "Ljava/lang/Long;";
                    } else if ("Short".equals(s0)) {
                        type[i] = "Ljava/lang/Short;";
                    } else if ("Boolean".equals(s0)) {
                        type[i] = "Ljava/lang/Boolean;";
                    } else if ("Character".equals(s0)) {
                        type[i] = "Ljava/lang/Character;";
                    } else {
                        if (!"Byte".equals(s0)) {
                            throw new CronModelException("Unknow parameter type, "
                                    + this.toString());
                        }

                        type[i] = "Ljava/lang/Byte;";
                    }
                }

                return type;
            }
        }

    }

    private String parseReturnType(String type) {
        String returnType;
        if ("VOID".equalsIgnoreCase(type)) {
            returnType = "V";
        } else if ("boolean".equals(type)) {
            returnType = "Z";
        } else if ("int".equals(type)) {
            returnType = "I";
        } else if ("String".equals(type)) {
            returnType = "Ljava/lang/String;";
        } else if ("float".equals(type)) {
            returnType = "F";
        } else if ("long".equals(type)) {
            returnType = "J";
        } else if ("double".equals(type)) {
            returnType = "D";
        } else if ("char".equals(type)) {
            returnType = "C";
        } else if ("byte".equals(type)) {
            returnType = "B";
        } else if ("short".equals(type)) {
            returnType = "S";
        } else if ("Integer".equals(type)) {
            returnType = "Ljava/lang/Integer;";
        } else if ("Long".equals(type)) {
            returnType = "Ljava/lang/Long;";
        } else if ("Short".equals(type)) {
            returnType = "Ljava/lang/Short;";
        } else if ("Double".equals(type)) {
            returnType = "Ljava/lang/Double;";
        } else if ("Float".equals(type)) {
            returnType = "Ljava/lang/Float;";
        } else if ("Byte".equals(type)) {
            returnType = "Ljava/lang/Byte;";
        } else if ("Character".equals(type)) {
            returnType = "Ljava/lang/Character;";
        } else {
            if (!"Boolean".equals(type)) {
                throw new CronModelException("Unknow return type, " + this.toString());
            }

            returnType = "Ljava/lang/Boolean;";
        }

        return returnType;
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

        Entity(String beanId, String className, String classType, String methodName,
                String[] variableValues, String[] variableTypes, String methodDesc,
                String returnType) {
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
            StringBuilder builder = new StringBuilder();
            builder.append("beanId=").append(this.beanId);
            builder.append(", className=").append(this.className);
            builder.append(", methodName=").append(this.methodName);
            builder.append(", variableValues=").append(Arrays.toString(this.variableValues));
            builder.append(", variableTypes=").append(Arrays.toString(this.variableTypes));
            builder.append(", methodDesc=").append(this.methodDesc);
            builder.append(", returnType=").append(this.returnType);
            return builder.toString();
        }
    }
}
