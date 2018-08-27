package net.zonia3000.jasmdecor;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.*;

/**
 * ClassVisitor used to generate a plain decorator class.
 *
 * @author zonia3000
 */
public class DecoratorClassAdapter extends ClassVisitor {

    private static final Logger LOG = Logger.getLogger(DecoratorClassAdapter.class.getCanonicalName());

    private final String decoratorName;
    private final String decoratorInternalName;
    private final Class wrappedType;
    private final String wrappedInternalName;

    // avoids writing duplicated methods (this can happen when multiple visits
    // of an overridden method are performed following the hierarchy)
    private final Set<String> visitedMethods;

    // ensures that the constructor is visited only once (necessary when multiple
    // visits are performed)
    private boolean constructorGenerated;

    // current visiting class name (used for logging purposes)
    private String visitingClass;

    public DecoratorClassAdapter(String decoratorName, Class wrappedType, ClassWriter cw) {
        super(ASM6, cw);
        this.decoratorName = decoratorName;
        this.decoratorInternalName = decoratorName.replace(".", "/");
        this.wrappedType = wrappedType;
        this.wrappedInternalName = Type.getInternalName(wrappedType);
        this.visitedMethods = new HashSet<>();
    }

    private String getSuperType() {
        if (wrappedType.isInterface()) {
            return Type.getInternalName(Object.class);
        }
        return wrappedInternalName;
    }

    private String[] getInterfacesArray() {
        if (wrappedType.isInterface()) {
            return new String[]{wrappedInternalName};
        }
        return new String[]{};
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.visitingClass = name;

        super.visit(version, getAccessFlag(access), decoratorName, signature, getSuperType(), getInterfacesArray());

        if (!constructorGenerated) {
            // Creates the wrapped field
            visitField(ACC_PRIVATE + ACC_FINAL, "wrapped", "L" + wrappedInternalName + ";", null, null);
            // Starts the constructor generation
            visitMethod(ACC_PUBLIC, "<init>", "(L" + wrappedInternalName + ";)V", null, null);
            constructorGenerated = true;
        }
    }

    /**
     * Decorator generated methods can be only public or protected.
     */
    private int getAccessFlag(int access) {
        if ((access & ACC_PROTECTED) == ACC_PROTECTED) {
            return ACC_PROTECTED;
        }
        return ACC_PUBLIC;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (skipMethod(access, name, descriptor)) {
            return null;
        }
        visitedMethods.add(name + descriptor);

        MethodVisitor mv = super.visitMethod(ACC_PUBLIC, name, descriptor, signature, exceptions);

        if ("<init>".equals(name)) { // build constructor
            LOG.log(Level.FINE, "Creating plain decorator constructor");
            mv = new DecoratorConstructorMethodAdapter(decoratorInternalName, wrappedType, mv);
            mv.visitCode();
        } else { // build delegation method
            LOG.log(Level.FINE, "Creating delegation method for {0}:{1}", new Object[]{name, descriptor});
            mv = new DelegationMethodAdapter(decoratorInternalName, wrappedType, mv, name, descriptor);
            // If the original method is abstract call visitCode()
            if ((access & ACC_ABSTRACT) == ACC_ABSTRACT) {
                mv.visitCode();
            }
        }

        return mv;
    }

    private boolean skipMethod(int access, String name, String descriptor) {

        boolean isFinal = (access & ACC_FINAL) == ACC_FINAL;

        if (isFinal && !visitingClass.equals(Type.getInternalName(Object.class))) {
            LOG.log(Level.WARNING, "Ignored final method {0}.{1}:{2}", new Object[]{visitingClass, name, descriptor});
            return true;
        }

        return (// undecorable methods
                access & (ACC_FINAL | ACC_STATIC | ACC_PRIVATE | ACC_NATIVE)) != 0
                // constructors
                || (constructorGenerated && "<init>".equals(name))
                // already visited methods
                || visitedMethods.contains(name + descriptor)
                // finalize Object method
                || ("finalize".equals(name) && "()V".equals(descriptor));
    }
}
