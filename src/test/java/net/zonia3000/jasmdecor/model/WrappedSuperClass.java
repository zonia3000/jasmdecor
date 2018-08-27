package net.zonia3000.jasmdecor.model;

import java.io.IOException;

/**
 * Class used for testing generation of inherited methods.
 *
 * @author @zonia3000
 */
public class WrappedSuperClass implements SuperIface {

    private boolean actionDone;

    @Override
    public void doAction() throws IOException {
        actionDone = true;
    }

    public boolean isActionDone() {
        return actionDone;
    }
}
