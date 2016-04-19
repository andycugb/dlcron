package com.andycugb.cron;

import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;

/**
 * Created by jbcheng on 2016-04-18.
 */
public class InitMethodAdapter extends MethodVisitor implements Opcodes {
    private String superClassName;

    public InitMethodAdapter(MethodVisitor visitor, String superClassName) {
        super(0, visitor);
        this.superClassName = superClassName;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (this.superClassName != null && opcode == 183 && "<init>".equals(name)) {
            owner = this.superClassName;
        }
        super.visitMethodInsn(opcode, owner, name, desc);
    }
}
