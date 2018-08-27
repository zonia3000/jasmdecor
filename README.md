# jasmdecor

**jasmdecor** is a decorator generator based on Java ASM bytecode manipulation framework.

It takes compiled classes as input and produces compiled decorator classes as output.

## Usage

### Plain decorator

```java
public interface Iface {

    void aMethod();
}
```

```java
new DecoratorGenerator(Iface.class, "PlainDecorator").writeDecoratorClass(outputFile);
```

Generates the following class:

```java
public class PlainDecorator implements Iface {

    private final Iface wrapped;

    public PlainDecorator(Iface wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void aMethod() {
        this.wrapped.aMethod();
    }
}
```

### Template based decorator

Starting from a template class:

```java
public class DecoratorFromTemplate {

    private final Iface wrapped;

    public Decorator(Iface wrapped) {
        this.wrapped = wrapped;
    }

    public void newMethod() {
        System.out.println("This is a new method!");
    }
}
```

**Please note**: it is not necessary for the template implementing the interface, but it is mandatory setting the wrapped field and name it `wrapped`. The template can introduce new methods or override methods of the wrapped instance class. The template can also be abstract if you need to add the `implements` part on the class definition without implementing all the methods.


```java
new DecoratorGenerator(Iface.class, DecoratorFromTemplate.class).writeDecoratorClass(outputFile);
```

Generates the following class:

```java
public class DecoratorFromTemplate implements Iface {

    private final Iface wrapped;

    public Decorator(Iface wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void aMethod() {
        this.wrapped.aMethod();
    }

    public void newMethod() {
        System.out.println("This is a new method!");
    }
}
```

### CLI

This project contains also a command line interface.

    java -jar jasmdecor.jar <class-to-decorate> <decorator-name-or-template> <output-file>

Example:

    java -jar target/jasmdecor-1.0.jar java.sql.PreparedStatement PSDecor PSDecor.class

## Additional notes

* Decorators can also be generated from non-final classes
* Final, native and static methods will be skipped
* Class inheritance is considered, so delegated methods of all the class hierarchy will be built (including decorable `Object` methods).
