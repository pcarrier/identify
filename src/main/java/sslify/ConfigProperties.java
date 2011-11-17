package sslify;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class ConfigProperties extends Properties {
    public static class ConfigLoadingException extends RuntimeException {
        public ConfigLoadingException(final String fullName) {
            super(fullName);
        }
    }

    public static final String
            LDAP = "ldap",
            REPO = "repo",
            X509 = "x509";

    private ConfigProperties() {
        super();
    }

    ;

    private static final HashMap<String, ConfigProperties> loaded = new HashMap<String, ConfigProperties>();

    static public ConfigProperties getProperties(final String name) throws ConfigLoadingException {
        if (loaded.containsKey(name)) {
            return loaded.get(name);
        } else {
            final ConfigProperties props = new ConfigProperties();
            final String fullName = name + ".conf.properties";
            final InputStream inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream(fullName);
            if (inputStream == null)
                throw new ConfigLoadingException(fullName);
            try {
                props.load(inputStream);
            } catch (IOException e) {
                throw new ConfigLoadingException(fullName);
            }
            loaded.put(name, props);
            return props;
        }
    }
}
