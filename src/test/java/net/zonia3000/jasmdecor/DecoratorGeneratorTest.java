package net.zonia3000.jasmdecor;

import net.zonia3000.jasmdecor.template.ConcreteDecorator;
import net.zonia3000.jasmdecor.template.AnotherIface;
import java.io.IOException;
import net.zonia3000.jasmdecor.model.ConcreteWrapped;
import net.zonia3000.jasmdecor.model.WrappedImpl;
import org.junit.Test;
import static org.junit.Assert.*;
import net.zonia3000.jasmdecor.model.Iface;
import net.zonia3000.jasmdecor.template.AbstractDecorator;

/**
 * Tests the various generation cases.
 *
 * @author @zonia3000
 */
public class DecoratorGeneratorTest {

    @Test
    public void testNoTemplate() throws Exception {
        Iface wrapped = new WrappedImpl();
        Iface decorator = (Iface) Class.forName("NoTemplate").getConstructor(Iface.class).newInstance(wrapped);
        assertEquals(wrapped.getString(), decorator.getString());
        assertEquals(wrapped.getInt(null), decorator.getInt(null));
        testExceptionInAction(decorator);
    }

    @Test
    public void testNoTemplateWithConcreteWrapped() throws Exception {
        ConcreteWrapped wrapped = new ConcreteWrapped();
        ConcreteWrapped decorator = (ConcreteWrapped) Class.forName("NoTemplateWithConcreteWrapped").getConstructor(ConcreteWrapped.class).newInstance(wrapped);
        assertEquals(wrapped.getString(), decorator.getString());
        wrapped.doAction();
        decorator.doAction();
        assertEquals(wrapped.isActionDone(), decorator.isActionDone());
    }

    @Test
    public void testWithAbstractTemplate() throws Exception {
        Iface wrapped = new WrappedImpl();
        Iface decorator = (Iface) Class.forName(AbstractDecorator.class.getName()).getConstructor(Iface.class).newInstance(wrapped);
        assertEquals(wrapped.getString(), decorator.getString());
        assertEquals(AbstractDecorator.NEW_VALUE, decorator.getInt(null));
        testExceptionInAction(decorator);
    }

    @Test
    public void testWithConcreteTemplate() throws Exception {
        Iface wrapped = new WrappedImpl();
        Iface decorator = (Iface) new ConcreteDecorator(wrapped, null);

        assertEquals(ConcreteDecorator.NEW_VALUE, decorator.getString());
        assertEquals(ConcreteDecorator.ANOTHER_VALUE, ((AnotherIface) decorator).anotherMethod());
        assertEquals(wrapped.getInt(null), decorator.getInt(null));
        testExceptionInAction(decorator);
    }

    private void testExceptionInAction(Iface iface) {
        boolean exceptionCaught = false;
        try {
            iface.doAction();
        } catch (IOException e) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);
    }
}
