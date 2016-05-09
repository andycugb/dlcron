package com.andycugb.cron;

import com.andycugb.cron.db.CronJobDao;
import com.andycugb.cron.db.CronJobModel;
import com.andycugb.cron.db.QuartzManager;
import com.andycugb.cron.util.Constant;
import com.andycugb.cron.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * Created by jbcheng on 2016-03-18.
 */
@Service
public class CronJobGenerator extends AbstractCronJob {
    private CronJobModel cron;
    @Autowired
    private CronJobDao cronJobJdbc;
    @Autowired
    private QuartzManager quartzManager;

    private CronJobGenerator() {

    }

    public static CronJobGenerator getInstance() {
        return ClassHolder.INSTANCE;
    }

    @Override
    public String doJob() {
        String desc = "no job found";
        if (null != cron) {
            ExecMethodDesc execMethod = cron.getExecMethod();
            Object obj = Constant.APP_CONTEXT.getBean(execMethod.getBeanId());
            if (null == obj) {
                Constant.LOG_CRON.error("no bean find in spring container:"
                        + execMethod.getBeanId());
                return desc;
            }
            Class clazz = obj.getClass();
            try {
                Method method =
                        clazz.getDeclaredMethod(execMethod.getMethodName(),
                                this.parseParamClazz(execMethod.getVariableTypes()));
                Object msg = method.invoke(clazz, new Object[] {execMethod.getVariableValues()});
                if (null != msg) {
                    desc = "success fired job:" + cron.getExecMethod().toString();
                }
            } catch (NoSuchMethodException e) {
                Constant.LOG_CRON.error("no such method:" + execMethod.getMethodName()
                        + ",find in class:" + clazz.getName());
            } catch (Exception e) {
                Constant.LOG_CRON.error(e.getMessage());
            }
        }
        return desc;
    }

    /**
     * invoke cron job.
     *
     * @param cron cron to invoke
     * @param type call type
     * @param checkIp check ip
     * @return exec status String
     * @throws Exception exec exception
     */
    public String executeJob(CronJobModel cron, String type, boolean checkIp) throws Exception {
        this.cron = cron;
        setCronJobJdbc(cronJobJdbc);
        setQuartzManager(quartzManager);
        return checkWhenDoTask(cron.getCronName(), type, Boolean.valueOf(checkIp),
                DateUtil.getCurrentTimestamp());
    }

    static class ClassHolder {
        private static final CronJobGenerator INSTANCE = new CronJobGenerator();
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
                            throw new CronModelException("unknown parameter type, "
                                    + this.toString());
                        }
                        clazz[i] = Character.class;
                    }
                }
                return clazz;
            }
        }
    }
}
