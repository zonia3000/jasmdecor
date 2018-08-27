package net.zonia3000.jasmdecor.model;

/**
 * Class used for testing the decoration of a concrete (not interface) class.
 *
 * @author @zonia3000
 */
public class ConcreteWrapped extends WrappedSuperClass {

    public static String VALUE = "ConcreteWrapped value";

    public String getString() {
        return VALUE;
    }
}
