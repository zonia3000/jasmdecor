package net.zonia3000.jasmdecor.template;

import net.zonia3000.jasmdecor.model.Iface;

/**
 * Abstract decorator template used for testing.
 * 
 * @author @zonia3000
 */
public abstract class AbstractDecorator implements Iface {

    public static final int NEW_VALUE = 5;

    private final Iface wrapped;

    public AbstractDecorator(Iface wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public int getInt(String fooParam) {
        return NEW_VALUE;
    }
}
