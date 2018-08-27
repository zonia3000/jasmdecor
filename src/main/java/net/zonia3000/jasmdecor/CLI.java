package net.zonia3000.jasmdecor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Command Line Interface.
 *
 * @author @zonia3000
 */
public class CLI {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        new CLI(args).execute();
    }

    private boolean debug;
    private Class wrappedType;
    private String decoratorName;
    private Class templateType;
    private File outputFile;

    private CLI(String[] args) {
        processArgs(args);
    }

    private void execute() throws IOException {
        setLogging();
        DecoratorGenerator generator = getDecoratorGenerator();
        generator.writeDecoratorClass(outputFile);
    }

    private void processArgs(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage: java -jar jasmdecor.jar <class-to-decorate> <decorator-name-or-template> <output-file>");
            System.exit(0);
        }

        for (String arg : args) {
            if (arg.startsWith("--")) {
                if ("--debug".equals(arg)) {
                    debug = true;
                } else {
                    System.err.println("Unrecognized flag " + arg);
                    System.exit(1);
                }
                continue;
            }

            if (wrappedType == null) {
                wrappedType = getClass(arg);
                if (wrappedType == null) {
                    System.err.println("Unable to find class to decorate: " + arg);
                    System.exit(1);
                }
                continue;
            }
            if (decoratorName == null) {
                decoratorName = arg;
                templateType = getClass(decoratorName);
                continue;
            }
            if (outputFile == null) {
                outputFile = getOutputFile(arg);
            }
        }
    }

    private Class getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private DecoratorGenerator getDecoratorGenerator() {
        if (templateType == null) {
            return new DecoratorGenerator(wrappedType, decoratorName);
        } else {
            return new DecoratorGenerator(wrappedType, templateType);
        }
    }

    /**
     * Get the output file, creating it if it doesn't exist.
     */
    private File getOutputFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            boolean fileCreated;
            try {
                fileCreated = file.createNewFile();
            } catch (IOException e) {
                fileCreated = false;
            }
            if (!fileCreated) {
                System.err.println("Unable to create file: " + file.getAbsolutePath());
                System.exit(1);
            }
        }
        return file;
    }

    private void setLogging() {
        Logger rootLog = LogManager.getLogManager().getLogger("");

        // use System.out
        rootLog.setUseParentHandlers(false);

        Handler handler = rootLog.getHandlers()[0];
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                // keeps only essential information
                return formatMessage(record) + "\n";
            }
        });

        if (debug) {
            rootLog.setLevel(Level.ALL);
            handler.setLevel(Level.ALL);
        }
    }
}
