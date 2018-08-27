package net.zonia3000.jasmdecor;

import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.Type;

/**
 * Generates a plain decorator constructor body
 * (<code>this.wrapped = wrapped;</code>).
 *
 * @author zonia3000
 */
public class DecoratorConstructorMethodAdapter extends MethodVisitor {

    private final String decoratorInternalName;
    private final Class wrappedType;

    public DecoratorConstructorMethodAdapter(String decoratorInternalName, Class wrappedType, MethodVisitor mv) {
        super(ASM6, mv);
        this.decoratorInternalName = decoratorInternalName;
        this.wrappedType = wrappedType;
    }

    @Override
    public void visitCode() {
        // put this on the stack
        visitVarInsn(ALOAD, 0);
        // invoke super constructor
        visitMethodInsn(INVOKESPECIAL, Type.getInternalName(wrappedType.isInterface() ? Object.class : wrappedType), "<init>", "()V", false);

        // put this on the stack
        visitVarInsn(ALOAD, 0);
        // put wrapped on the stack
        visitVarInsn(ALOAD, 1);
        // set the wrapped field
        visitFieldInsn(PUTFIELD, decoratorInternalName, "wrapped", "L" + Type.getInternalName(wrappedType) + ";");

        visitInsn(RETURN);

        visitMaxs(2, 2);
        visitEnd();
    }
}
