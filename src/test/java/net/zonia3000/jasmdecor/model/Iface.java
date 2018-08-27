package net.zonia3000.jasmdecor.model;

import java.io.IOException;

/**
 * Interface used for testing the generation of decorators.
 *
 * @author zonia3000
 */
public interface Iface extends SuperIface {

    String getString();

    int getInt(String fooParam);

    void variousDataTypes(byte b1, short s1, int[] a1, int[][] a2, String[][] a3, boolean b, float f, char c, long l);

    default String defaultMethod(int a, int b, int c) throws IOException {
        throw new UnsupportedOperationException("Not supported yet");
    }
}
