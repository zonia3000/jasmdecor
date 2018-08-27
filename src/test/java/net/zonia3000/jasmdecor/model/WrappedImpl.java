package net.zonia3000.jasmdecor.model;

import java.io.IOException;

/**
 * Instances of this class will be wrapped by decorators during tests.
 *
 * @author zonia3000
 */
public class WrappedImpl implements Iface {

    public static final String STRING_VALUE = "WrappedImpl value";
    public static final int INT_VALUE = 3;

    @Override
    public String getString() {
        return STRING_VALUE;
    }

    @Override
    public int getInt(String fooParam) {
        return INT_VALUE;
    }

    @Override
    public void doAction() throws IOException {
        throw new IOException();
    }

    @Override
    public void variousDataTypes(byte b1, short s1, int[] a1, int[][] a2, String[][] a3, boolean b, float f, char c, long l) {
    }
}
