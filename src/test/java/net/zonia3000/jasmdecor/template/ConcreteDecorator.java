package net.zonia3000.jasmdecor.template;

import net.zonia3000.jasmdecor.model.Iface;

/**
 * Concrete decorator template used for testing.
 *
 * @author @zonia3000
 */
public class ConcreteDecorator implements AnotherIface {

    public static final String NEW_VALUE = "ConcreteDecorator new value";
    public static final String ANOTHER_VALUE = "another value";

    private final Iface wrapped;

    public ConcreteDecorator(Iface wrapped, String foo) {
        this.wrapped = wrapped;
    }

    public String getString() {
        return NEW_VALUE;
    }

    @Override
    public String anotherMethod() {
        return ANOTHER_VALUE;
    }
}
