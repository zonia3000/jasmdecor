package net.zonia3000.jasmdecor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 * Can generate a decorator from scratch (plain decorator) or using a template
 * class. When using the template class the generated decorator will have the
 * same name of the template. The template must have a constructor which
 * initialize a field named "wrapped" storing the delegated object. Methods
 * overridden by the template will be left unchanged in the generated decorator.
 * Final and native methods will be ignored.
 *
 * @author zonia3000
 */
public class DecoratorGenerator {

    private static final Logger LOG = Logger.getLogger(DecoratorGenerator.class.getName());

    private final Class wrappedType;
    private final String decoratorInternalName;

    private Class templateType;
    private ClassLoader classLoader;

    /**
     * Initializes a generator for a decorator having no template class.
     *
     * @param wrappedType the class/interface to decorate
     * @param decoratorCompleteName the name of the class to generate, including
     * also the package name
     */
    public DecoratorGenerator(Class wrappedType, String decoratorCompleteName) {
        this.wrappedType = wrappedType;
        this.decoratorInternalName = decoratorCompleteName.replace(".", "/");
    }

    /**
     * Initializes a generator for a decorator having no template class.
     *
     * @param wrappedType the class/interface to decorate
     * @param decoratorCompleteName the name of the class to generate, including
     * also the package name
     * @param classLoader the ClassLoader to use for loading the decorated class
     * (this is useful because ASM ClassReader(String className) uses always the
     * system ClassLoader).
     */
    public DecoratorGenerator(Class wrappedType, String decoratorCompleteName, ClassLoader classLoader) {
        this(wrappedType, decoratorCompleteName);
        this.classLoader = classLoader;
    }

    /**
     * Initializes a generator for a decorator based on a template class.
     *
     * @param wrappedType the class/interface to decorate
     * @param templateType a class to use as a template for building the
     * decorator
     */
    public DecoratorGenerator(Class wrappedType, Class templateType) {
        this(wrappedType, templateType.getCanonicalName());
        this.templateType = templateType;
    }

    /**
     * Initializes a generator for a decorator based on a template class.
     *
     * @param wrappedType the class/interface to decorate
     * @param templateType a class to use as a template for building the
     * decorator
     * @param classLoader the ClassLoader to use for loading the decorated class
     * (this is useful because ASM ClassReader(String className) uses always the
     * system ClassLoader).
     */
    public DecoratorGenerator(Class wrappedType, Class templateType, ClassLoader classLoader) {
        this(wrappedType, templateType.getCanonicalName());
        this.templateType = templateType;
        this.classLoader = classLoader;
    }

    /**
     * Generates the decorator class and writes it inside the specified file.
     *
     * @param file the file where write the class bytes
     * @throws IOException
     */
    public void writeDecoratorClass(File file) throws IOException {
        if (templateType == null) {
            LOG.log(Level.INFO, "Starting the generation of a plain decorator class");
        } else {
            LOG.log(Level.INFO, "Starting the generation of a decorator class based on {0} template", templateType.getCanonicalName());
        }

        byte[] generatedClassBytes = getDecoratorBytes();

        checkIfClassIsValid(generatedClassBytes);

        LOG.log(Level.INFO, "Writing class file to {0}", file.getAbsolutePath());

        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(generatedClassBytes);
        }
    }

    private byte[] getDecoratorBytes() throws IOException {
        ClassWriter classWriter = new ClassWriter(0);

        DecoratorClassAdapter classAdapter = new DecoratorClassAdapter(decoratorInternalName, wrappedType, classWriter);

        // Read all classes/interfaces of the wrapped type hierarchy
        for (Class type : getHierarchy()) {
            LOG.log(Level.FINE, "Reading class {0}", type.getCanonicalName());
            ClassReader classReader = getClassReader(type);
            classReader.accept(classAdapter, 0);
        }

        byte[] classBytes = classWriter.toByteArray();

        if (templateType != null) {
            classBytes = writeTemplate(classBytes);
        }

        return classBytes;
    }

    /**
     * @return the set of classes from which implement the decorator methods.
     * The order of the elements in the set matters.
     */
    private Set<Class> getHierarchy() {
        Set<Class> hierarchy = new LinkedHashSet<>();
        fillHierarchy(wrappedType, hierarchy);
        if (wrappedType.isInterface()) {
            hierarchy.add(Object.class);
        }
        return hierarchy;
    }

    private void fillHierarchy(Class type, Set<Class> hierarchy) {
        hierarchy.add(type);
        if (type.getSuperclass() != null) {
            fillHierarchy(type.getSuperclass(), hierarchy);
        }
        for (Class iface : type.getInterfaces()) {
            fillHierarchy(iface, hierarchy);
        }
    }

    private ClassReader getClassReader(Class type) throws IOException {
        return getClassReader(type.getCanonicalName());
    }

    private ClassReader getClassReader(String className) throws IOException {
        if (classLoader == null) {
            return new ClassReader(className);
        } else {
            return new ClassReader(getClassBytesFromClassLoader(className));
        }
    }

    private byte[] getClassBytesFromClassLoader(String className) throws IOException {
        try (InputStream in = classLoader.getResourceAsStream(className.replace(".", File.separator) + ".class");
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                baos.write(data, 0, nRead);
            }
            return baos.toByteArray();
        }
    }

    private byte[] writeTemplate(byte[] classBytes) throws IOException {
        ClassWriter classWriter = new ClassWriter(COMPUTE_MAXS + COMPUTE_FRAMES);

        // First it visits the template class
        TemplateClassAdapter templateClassAdapter = new TemplateClassAdapter(wrappedType, classWriter);
        LOG.log(Level.FINE, "Reading template class {0}", templateType.getCanonicalName());
        ClassReader templateClassReader = getClassReader(templateType);
        templateClassReader.accept(templateClassAdapter, 0);
        templateClassAdapter.setTemplateVisited();

        // Then it vists the generated plain decorator class. In this way we can
        // exclude the template methods from the plain decorator methods.
        LOG.log(Level.FINE, "Reading generated plain decorator class");
        ClassReader generatedBytesClassReader = new ClassReader(classBytes);
        generatedBytesClassReader.accept(templateClassAdapter, 0);

        return classWriter.toByteArray();
    }

    /**
     * Uses the ASM CheckClassAdapter to verify if the generated decorator class
     * is valid.
     *
     * @param classBytes the bytes of the generated decorator class
     */
    private void checkIfClassIsValid(byte[] classBytes) {
        LOG.log(Level.FINE, "Checking class structure");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        CheckClassAdapter.verify(new ClassReader(classBytes), false, printWriter);
        String verificationOutput = stringWriter.toString();
        if (!verificationOutput.isEmpty()) {
            throw new RuntimeException(verificationOutput);
        }
    }
}
