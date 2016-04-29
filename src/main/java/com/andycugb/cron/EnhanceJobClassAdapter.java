package com.andycugb.cron;

import com.andycugb.cron.db.CronJobModel;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;

/**
 * Created by jbcheng on 2016-04-18.
 */
public class EnhanceJobClassAdapter extends ClassVisitor implements Opcodes {
    private CronJobModel cron;
    private String enhancedSuperName = null;

    public EnhanceJobClassAdapter(ClassVisitor visitor, CronJobModel cron) {
        super(0, visitor);
        this.cron = cron;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
            String[] interfaces) {
        String enhancedName = name + "$EnhancedByASM$" + this.cron.getEnhancedSubClassSuffix();
        this.enhancedSuperName = name;
        this.cv.visit(version, 1, enhancedName, signature, this.enhancedSuperName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
            String[] exception) {
        if (!"doJob".equals(name) && !"<init>".equals(name)) {
            return null;
        } else {
            if (access == 1028) {
                access = 1;
            }
            MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exception);
            Object wrapped = mv;
            if (mv != null) {
                if ("doJob".equals(name)) {
                    wrapped = new DoJobMethodAdapter(mv, this.cron);
                } else if ("<init>".equals(name)) {
                    wrapped = new InitMethodAdapter(mv, this.enhancedSuperName);
                }
            }
            return (MethodVisitor) wrapped;
        }
    }
}
