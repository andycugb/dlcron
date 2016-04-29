package com.andycugb.cron;

import com.andycugb.cron.db.CronJobModel;
import com.andycugb.cron.util.Constant;
import com.andycugb.cron.util.DateUtil;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassWriter;
import org.springframework.asm.Opcodes;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jbcheng on 2016-03-18.
 */
public class ClassGenerator implements Opcodes {

    private static final ProtectionDomain PROTECTION_DOMAIN;
    private static Method DEFINE_CLASS;
    private static ClassLoader CLASSLOADER;
    private static Class<?>[] CRON_CLASSES;
    private static volatile Map<String, Class<?>> LOADED_CLASS = new HashMap<String, Class<?>>();

    static {
        CRON_CLASSES = new Class[] {String.class, String.class, Boolean.class, Timestamp.class};
        PROTECTION_DOMAIN =
                AccessController.doPrivileged(new PrivilegedAction<ProtectionDomain>() {
                    public ProtectionDomain run() {
                        return this.getClass().getProtectionDomain();
                    }
                });
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    Class clazz = ClassLoader.class;
                    ClassGenerator.DEFINE_CLASS =
                            clazz.getDeclaredMethod("defineClass", new Class[] {String.class,
                                    byte[].class, Integer.TYPE, Integer.TYPE,
                                    ProtectionDomain.class});
                    ClassGenerator.DEFINE_CLASS.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }

    private ClassGenerator() {

    }

    /**
     * singleton instance.
     * 
     * @return instance of ClassGenerator
     */
    public static ClassGenerator getInstance() {
        return ClassHolder.INSTANCE;
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
    public static String executeJob(CronJobModel cron, String type, boolean checkIp)
            throws Exception {
        Class clazz = getClazz(cron);
        Object obj = clazz.newInstance();
        Method method = clazz.getMethod("checkWhenDoTask", CRON_CLASSES);
        return (String) method.invoke(
                obj,
                new Object[] {cron.getCronName(), type, Boolean.valueOf(checkIp),
                        DateUtil.getCurrentTimestamp()});
    }

    /**
     * get class by cron.
     * 
     * @param cron cron instance
     * @return Class instance
     */
    public static Class<?> getClazz(CronJobModel cron) {
        Class clazz = null;
        String className =
                "com.andycugb.cron.AbstractJob$EnhancedByASM$" + cron.getEnhancedSubClassSuffix();
        try {
            if (LOADED_CLASS.containsKey(className)) {
                clazz = getClassLoader().loadClass(className);
            } else {
                synchronized (LOADED_CLASS) {
                    if (LOADED_CLASS.containsKey(className)) {
                        clazz = getClassLoader().loadClass(className);
                    } else {
                        byte[] code = compile(cron);
                        if (code != null) {
                            clazz =
                                    (Class) DEFINE_CLASS.invoke(getClassLoader(), new Object[] {
                                            className, code, new Integer[0],
                                            new Integer(code.length), PROTECTION_DOMAIN});
                            if (clazz != null) {
                                LOADED_CLASS.put(className, clazz);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Constant.LOG_CRON.error("Fail to load class, cron=" + cron.toString(), e);
        }
        return clazz;
    }

    private static ClassLoader getClassLoader() {
        if (CLASSLOADER == null) {
            CLASSLOADER = Constant.APP_CONTEXT.getClassLoader();
        }
        return CLASSLOADER;
    }

    private static byte[] compile(CronJobModel cron) {
        String resource = "AbstractCronJob.class";
        InputStream stream = ClassGenerator.class.getResourceAsStream(resource);
        byte[] result = null;

        try {
            ClassReader reader = new ClassReader(stream);
            ClassWriter writer = new ClassWriter(1);
            EnhanceJobClassAdapter classAdapter = new EnhanceJobClassAdapter(writer, cron);
            reader.accept(classAdapter, 2);
            result = writer.toByteArray();
        } catch (Throwable e) {
            Constant.LOG_CRON.error("Fail to get class byte array, cron=" + cron.toString(), e);
        }
        return result;
    }

    static class ClassHolder {
        private static ClassGenerator INSTANCE = new ClassGenerator();
    }
}
