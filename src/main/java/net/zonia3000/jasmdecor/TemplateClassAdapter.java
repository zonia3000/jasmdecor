package net.zonia3000.jasmdecor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ASM6;
import org.objectweb.asm.Type;

/**
 * ClassVisitor used to merge the template class with the plain decorator class.
 *
 * @author @zonia3000
 */
public class TemplateClassAdapter extends ClassVisitor {

    private static final Logger LOG = Logger.getLogger(TemplateClassAdapter.class.getCanonicalName());

    // interfaces implemented by the generated decorator
    private final Set<String> interfaces;

    // this is used to avoid writing plain decorator methods when they have been
    // defined in the template
    private final Set<String> visitedMethods;

    private boolean templateVisited;

    public TemplateClassAdapter(Class wrappedType, ClassWriter cw) {
        super(ASM6, cw);
        interfaces = new HashSet<>();
        visitedMethods = new HashSet<>();
        if (wrappedType.isInterface()) {
            interfaces.add(Type.getInternalName(wrappedType));
        }
    }

    public void setTemplateVisited() {
        this.templateVisited = true;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (templateVisited && "wrapped".equals(name)) {
            return null;
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (!templateVisited) {
            // Add interfaces defined on the template
            addInterfaces(interfaces);
            // visit the template class
            super.visit(version, ACC_PUBLIC, name, signature, superName, getInterfaces());
        }
    }

    private void addInterfaces(String[] interfacesArray) {
        interfaces.addAll(Arrays.asList(interfacesArray));
    }

    /**
     * Returns an arrays of interface internal names from the set.
     */
    private String[] getInterfaces() {
        String[] newArray = new String[interfaces.size()];
        int i = 0;
        for (String iFace : interfaces) {
            newArray[i++] = iFace;
        }
        return newArray;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (visitedMethods.contains(name + descriptor) /* already visited method */
                || (templateVisited && "<init>".equals(name)) /* constructor */) {
            // ignore the method
            return null;
        }
        visitedMethods.add(name + descriptor);
        // copy the method
        LOG.log(Level.FINE, "Copying method {0}:{1}", new Object[]{name, descriptor});
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}
