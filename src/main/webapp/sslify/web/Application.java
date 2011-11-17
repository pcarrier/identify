package sslify.web;

import java.util.HashSet;
import java.util.Set;

public class Application extends javax.ws.rs.core.Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(HelloWorld.class);
        return classes;
    }
}
