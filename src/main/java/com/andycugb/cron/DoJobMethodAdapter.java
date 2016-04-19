package com.andycugb.cron;

import com.andycugb.cron.db.CronJobModel;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;

import java.util.Iterator;
import java.util.List;

/**
 * Created by jbcheng on 2016-04-18.
 */
public class DoJobMethodAdapter extends MethodVisitor implements Opcodes {
    private CronJobModel cron;

    public DoJobMethodAdapter(MethodVisitor mv, CronJobModel cron) {
        super(0, mv);
        this.cron = cron;
    }

    @Override
    public void visitEnd() {
        this.visitCode();
        this.visitTypeInsn(187, "java/lang/StringBuilder");
        this.visitInsn(89);
        this.visitMethodInsn(183, "java/lang/StringBuilder", "<init>", "()V");
        this.visitVarInsn(58, 1);
        int maxVariableCount = 0;
        int totalReturnCount = 0;
        List<Inner> innerList = this.cron.getInnerList();
        int index = 2;
        Iterator<Inner> maxLocals = innerList.iterator();

        while (maxLocals.hasNext()) {
            Inner maxStack = maxLocals.next();
            this.visitFieldInsn(178, "com/andycugb/cron/Constant", "APP_CONTEXT",
                    "Lorg/springframework/context/ApplicationContext");
            this.visitLdcInsn(maxStack.getBeanId());
            this.visitMethodInsn(185, "org/springframework/context/ApplicationContext",
                    "getBean", "(Ljava/lang/String;)Ljava/lang/Object;");
            this.visitTypeInsn(192, maxStack.getClassName());
            this.visitVarInsn(58, index);
            this.mv.visitLdcInsn(maxStack.getBeanId() + "." + maxStack.getMethodName() + "("
                    + this.getParameterValues(maxStack.getVariableValues()) + ");");
            this.mv.visitMethodInsn(182, "java/lang/StringBuilder", "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            this.mv.visitInsn(87);
            this.visitVarInsn(25, index);
            String[] variableTypes = maxStack.getVariableTypes();
            String[] variableValues = maxStack.getVariableValues();

            int length = variableTypes.length;
            for (int i = 0; i < length; i++) {
                this.addVariable(variableTypes[i], variableValues[i]);
            }

            if (maxVariableCount < length) {
                maxVariableCount = length;
            }
            this.visitMethodInsn(182, maxStack.getClassName(), maxStack.getMethodName(),
                    maxStack.getMethodDesc());
            if ("V".equals(maxStack.getReturnType())) {
                this.mv.visitVarInsn(25, 1);
                this.mv.visitLdcInsn("void;");
                this.mv.visitMethodInsn(182, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                this.mv.visitInsn(87);
            } else {
                int var = this.getStoreType(maxStack.getReturnType());
                ++index;
                this.visitVarInsn(var, index);
                this.visitVarInsn(25, 1);
                this.visitVarInsn(this.getLoadType(maxStack.getReturnType()), index);
                this.visitMethodInsn(182, "java/lang/StringBuilder", "append",
                        "(" + this.getStringBuilderAppendType(maxStack.getReturnType())
                                + ")Ljava/lang/StringBuilder;");
                this.visitInsn(87);
                this.mv.visitVarInsn(25, 1);
                this.mv.visitLdcInsn(";");
                this.mv.visitMethodInsn(182, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                this.mv.visitInsn(87);
                ++totalReturnCount;
            }
        }
        this.visitVarInsn(25, 1);
        this.visitMethodInsn(182, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
        this.visitInsn(176);
        int i = 2 + innerList.size() + maxVariableCount;
        int j = 2 + innerList.size() + totalReturnCount;
        this.visitMaxs(i, j);
    }

    private void addVariable(String type, String value) {
        if ("Z".equals(type)) {
            if ("TRUE".equalsIgnoreCase(value)) {
                this.visitInsn(4);
            } else {
                if (!"FALSE".equalsIgnoreCase(value)) {
                    throw new RuntimeException("Incorrect boolean value, type=" + type
                            + ", value=" + value);
                }

                this.visitInsn(3);
            }
        } else if ("I".equals(type)) {
            this.visitLdcInsn(new Integer(value));
        } else if ("Ljava/lang/String;".equals(type)) {
            this.visitLdcInsn(value);
        } else if ("F".equals(type)) {
            this.visitLdcInsn(new Float(value));
        } else if ("J".equals(type)) {
            this.visitLdcInsn(new Long(value));
        } else if ("D".equals(type)) {
            this.visitLdcInsn(new Double(value));
        } else if ("S".equals(type)) {
            this.visitLdcInsn(new Short(value));
        } else if ("B".equals(type)) {
            this.visitLdcInsn(new Byte(value));
        } else if ("C".equals(type)) {
            this.visitLdcInsn(Character.valueOf(value.charAt(0)));
        } else if ("Ljava/lang/Integer;".equals(type)) {
            if ("NULL".equalsIgnoreCase(value)) {
                this.visitInsn(1);
            } else {
                this.visitLdcInsn(new Integer(value));
                this.visitMethodInsn(184, "java/lang/Integer", "valueOf",
                        "(I)Ljava/lang/Integer;");
            }
        } else if ("Ljava/lang/Float;".equals(type)) {
            if ("NULL".equalsIgnoreCase(value)) {
                this.visitInsn(1);
            } else {
                this.visitLdcInsn(new Float(value));
                this.visitMethodInsn(184, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
            }
        } else if ("Ljava/lang/Double;".equals(type)) {
            if ("NULL".equalsIgnoreCase(value)) {
                this.visitInsn(1);
            } else {
                this.visitLdcInsn(new Double(value));
                this.visitMethodInsn(184, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
            }
        } else if ("Ljava/lang/Short;".equals(type)) {
            if ("NULL".equalsIgnoreCase(value)) {
                this.visitInsn(1);
            } else {
                this.visitLdcInsn(new Short(value));
                this.visitMethodInsn(184, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
            }
        } else if ("Ljava/lang/Long;".equals(type)) {
            if ("NULL".equalsIgnoreCase(value)) {
                this.visitInsn(1);
            } else {
                this.visitLdcInsn(new Long(value));
                this.visitMethodInsn(184, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
            }
        } else if ("Ljava/lang/Boolean;".equals(type)) {
            if ("NULL".equalsIgnoreCase(value)) {
                this.visitInsn(1);
            } else {
                this.visitLdcInsn(new Boolean(value));
                this.visitMethodInsn(184, "java/lang/Boolean", "valueOf",
                        "(Z)Ljava/lang/Boolean;");
            }
        } else if ("Ljava/lang/Byte;".equals(type)) {
            if ("NULL".equalsIgnoreCase(value)) {
                this.visitInsn(1);
            } else {
                this.visitLdcInsn(new Byte(value));
                this.visitMethodInsn(184, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
            }
        } else {
            if (!"Ljava/lang/Character;".equals(type)) {
                throw new RuntimeException("Incorrect parameter type, type=" + type + ", value="
                        + value);
            }

            if ("NULL".equalsIgnoreCase(value)) {
                this.visitInsn(1);
            } else {
                this.visitLdcInsn(Character.valueOf(value.charAt(0)));
                this.visitMethodInsn(184, "java/lang/Character", "valueOf",
                        "(C)Ljava/lang/Character;");
            }
        }
    }

    private int getLoadType(String returnType) {
        if (!"Z".equals(returnType) && !"I".equals(returnType) && !"B".equals(returnType)
                && !"S".equals(returnType) && !"C".equals(returnType)) {
            if (!"Ljava/lang/String;".equals(returnType)
                    && !"Ljava/lang/Integer;".equals(returnType)
                    && !"Ljava/lang/Long;".equals(returnType)
                    && !"Ljava/lang/Boolean;".equals(returnType)
                    && !"Ljava/lang/Short;".equals(returnType)
                    && !"Ljava/lang/Float;".equals(returnType)
                    && !"Ljava/lang/Double;".equals(returnType)
                    && !"Ljava/lang/Character;".equals(returnType)
                    && !"Ljava/lang/Byte;".equals(returnType)) {
                if ("F".equals(returnType)) {
                    return 23;
                } else if ("J".equals(returnType)) {
                    return 22;
                } else if ("D".equals(returnType)) {
                    return 24;
                } else {
                    throw new RuntimeException("CronModel is incorrect, returnType=" + returnType);
                }
            } else {
                return 25;
            }
        } else {
            return 21;
        }
    }

    private int getStoreType(String returnType) {
        if (!"Z".equals(returnType) && !"I".equals(returnType) && !"B".equals(returnType)
                && !"S".equals(returnType) && !"C".equals(returnType)) {
            if (!"Ljava/lang/String;".equals(returnType)
                    && !"Ljava/lang/Integer;".equals(returnType)
                    && !"Ljava/lang/Long;".equals(returnType)
                    && !"Ljava/lang/Boolean;".equals(returnType)
                    && !"Ljava/lang/Short;".equals(returnType)
                    && !"Ljava/lang/Float;".equals(returnType)
                    && !"Ljava/lang/Double;".equals(returnType)
                    && !"Ljava/lang/Character;".equals(returnType)
                    && !"Ljava/lang/Byte;".equals(returnType)) {
                if ("F".equals(returnType)) {
                    return 56;
                } else if ("J".equals(returnType)) {
                    return 55;
                } else if ("D".equals(returnType)) {
                    return 57;
                } else {
                    throw new RuntimeException("CronModel is incorrect, returnType=" + returnType);
                }
            } else {
                return 58;
            }
        } else {
            return 54;
        }
    }

    private String getParameterValues(String[] parameterValues) {
        StringBuilder s = new StringBuilder();
        int i = 0;

        for (int n = parameterValues.length; i < n; ++i) {
            s.append(parameterValues[i]);
            if (i < n - 1) {
                s.append(",");
            }
        }

        return s.toString();
    }

    private String getStringBuilderAppendType(String returnType) {
        return !"Ljava/lang/Integer;".equals(returnType)
                && !"Ljava/lang/Long;".equals(returnType)
                && !"Ljava/lang/Boolean;".equals(returnType)
                && !"Ljava/lang/Short;".equals(returnType)
                && !"Ljava/lang/Float;".equals(returnType)
                && !"Ljava/lang/Double;".equals(returnType)
                && !"Ljava/lang/Character;".equals(returnType)
                && !"Ljava/lang/Byte;".equals(returnType) ? (!"S".equals(returnType)
                && !"B".equals(returnType) ? returnType : "I") : "Ljava/lang/Object;";
    }
}
