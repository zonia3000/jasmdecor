package net.zonia3000.jasmdecor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.*;

/**
 * Generates the body of delegation methods (e.g.
 * <code>return wrapped.aMethod(args);</code>).
 *
 * @author zonia3000
 */
public class DelegationMethodAdapter extends MethodVisitor {

    private final String decoratorInternalName;
    private final Class wrappedType;
    private final String wrappedInternalName;
    private final MethodVisitor writer;
    private final String methodName;
    private final String descriptor;

    public DelegationMethodAdapter(String wrapperInternalName, Class wrappedType, MethodVisitor writer, String methodName, String descriptor) {
        super(ASM6); // we don't want to pass the MethodVisitor in the super constructor, otherwise concrete method bodies will be visited
        this.decoratorInternalName = wrapperInternalName;
        this.wrappedType = wrappedType;
        this.wrappedInternalName = Type.getInternalName(wrappedType);
        this.writer = writer;
        this.methodName = methodName;
        this.descriptor = descriptor;
    }

    @Override
    public void visitCode() {
        // put this on the stack
        writer.visitVarInsn(ALOAD, 0);
        // get the wrapped field
        writer.visitFieldInsn(GETFIELD, decoratorInternalName, "wrapped", "L" + wrappedInternalName + ";");

        // put all method arguments on the stack
        int varIndex = addMethodArgumentsToStack(descriptor);
        // invoke delegated method
        writer.visitMethodInsn(wrappedType.isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL, wrappedInternalName, methodName, descriptor, wrappedType.isInterface());

        // return
        addReturnInsn(descriptor);
        writer.visitMaxs(varIndex + 1, varIndex + 1);
    }

    /**
     * Puts all the method arguments on the operand stack extracting them
     * parsing the method descriptor.
     *
     * @return the size of the stack
     */
    private int addMethodArgumentsToStack(String methodDescriptor) {

        String parametersPart = extractArgumentsPart(methodDescriptor);

        int varIndex = 0;
        boolean parsingClassNameParam = false;
        boolean parsingArrayParam = false;

        for (int i = 0; i < parametersPart.length(); i++) {
            char c = parametersPart.charAt(i);
            if (parsingClassNameParam) {
                if (c == ';') { // end of class name
                    parsingClassNameParam = false;
                }
            } else if (parsingArrayParam) {
                if (c != '[') { // one or more '[', then it ends with the array type
                    parsingArrayParam = false;
                    if (c == 'L') {
                        parsingClassNameParam = true;
                    }
                }
            } else {
                switch (c) {
                    case 'L': // start of class name
                        writer.visitVarInsn(ALOAD, ++varIndex);
                        parsingClassNameParam = true;
                        break;
                    case 'I': // int
                    case 'B': // byte
                    case 'S': // short
                    case 'C': // char
                    case 'Z': // boolean
                        writer.visitVarInsn(ILOAD, ++varIndex);
                        break;
                    case 'F': // float
                        writer.visitVarInsn(FLOAD, ++varIndex);
                        break;
                    case 'D': // double
                        writer.visitVarInsn(DLOAD, ++varIndex);
                        // doubles take 2 position
                        ++varIndex;
                        break;
                    case 'J': // long
                        writer.visitVarInsn(LLOAD, ++varIndex);
                        // longs take 2 position
                        ++varIndex;
                        break;
                    case '[': // array
                        writer.visitVarInsn(ALOAD, ++varIndex);
                        parsingArrayParam = true;
                        break;
                }
            }
        }

        return varIndex;
    }

    /**
     * Extracts the method arguments from method descriptor (the part inside
     * parenthesis).
     */
    private String extractArgumentsPart(String methodDescriptor) {
        Pattern pattern = Pattern.compile("[(](.*?)[)]");
        Matcher matcher = pattern.matcher(methodDescriptor);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException("Parameters group not found for method descriptor " + methodDescriptor);
        }
    }

    /**
     * Adds a proper return instruction according to original method data type.
     */
    private void addReturnInsn(String methodDescriptor) {
        // the return type is defined after method arguments in the descriptor
        int returnFirstCharIndex = methodDescriptor.indexOf(")") + 1;
        char returnFirstChar = methodDescriptor.charAt(returnFirstCharIndex);

        switch (returnFirstChar) {
            case 'V': // void
                writer.visitInsn(RETURN);
                break;
            case 'I': // int
            case 'B': // byte
            case 'S': // short
            case 'C': // char
            case 'Z': // boolean
                writer.visitInsn(IRETURN);
                break;
            case 'F': // float
                writer.visitInsn(FRETURN);
                break;
            case 'D': // double
                writer.visitInsn(DRETURN);
                break;
            case 'J': // long
                writer.visitInsn(LRETURN);
                break;
            default: // reference
                writer.visitInsn(ARETURN);
                break;
        }
    }
}
