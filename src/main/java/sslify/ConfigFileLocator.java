package sslify;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ConfigFileLocator {
    private static final String CONF_PATH_PROPERTY = "sslify.configpath";

    @NotNull
    static InputStream getInputStream(final String name) throws FileNotFoundException {
        final String confPath = System.getProperty(CONF_PATH_PROPERTY);
        final InputStream stream;
        if (confPath == null) {
            stream = ConfigFileLocator.class.getClassLoader().getResourceAsStream(name);
        } else {
            stream = new FileInputStream(new File(confPath, name));
        }
        return stream;
    }
}
