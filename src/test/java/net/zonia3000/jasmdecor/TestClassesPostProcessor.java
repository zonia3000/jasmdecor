package net.zonia3000.jasmdecor;

import java.io.File;
import java.io.IOException;
import net.zonia3000.jasmdecor.model.ConcreteWrapped;
import net.zonia3000.jasmdecor.model.Iface;
import net.zonia3000.jasmdecor.template.AbstractDecorator;
import net.zonia3000.jasmdecor.template.ConcreteDecorator;

/**
 * This Main Class generates the decorators used inside tests. It MUST be
 * executed after test sources have been compiled in order to execute tests
 * correctly.
 *
 * @author @zonia3000
 */
public class TestClassesPostProcessor {

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            throw new IllegalArgumentException("First argument must be the test classes directory");
        }

        TestClassesPostProcessor postProcessor = new TestClassesPostProcessor(args[0]);
        postProcessor.generateTestingDecorators();
    }

    private final File testClassesDir;

    private TestClassesPostProcessor(String testClassesDir) {
        this.testClassesDir = new File(testClassesDir);
        if (!this.testClassesDir.exists()) {
            throw new IllegalStateException("Test classes directory doesn't exists: " + this.testClassesDir.getAbsolutePath());
        }
    }

    private void generateTestingDecorators() throws IOException {

        ClassLoader classLoader = TestClassesPostProcessor.class.getClassLoader();

        writeClass(new DecoratorGenerator(Iface.class, "NoTemplate", classLoader),
                "NoTemplate");

        writeClass(new DecoratorGenerator(ConcreteWrapped.class, "NoTemplateWithConcreteWrapped", classLoader),
                "NoTemplateWithConcreteWrapped");

        writeClass(new DecoratorGenerator(Iface.class, AbstractDecorator.class, classLoader),
                AbstractDecorator.class.getCanonicalName());

        writeClass(new DecoratorGenerator(Iface.class, ConcreteDecorator.class, classLoader),
                ConcreteDecorator.class.getCanonicalName());
    }

    private void writeClass(DecoratorGenerator decorGen, String className) throws IOException {
        File file = testClassesDir.toPath()
                .resolve(className.replace(".", File.separator) + ".class")
                .toFile();

        decorGen.writeDecoratorClass(file);
    }
}
