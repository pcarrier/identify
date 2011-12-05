package sslify;

import java.io.*;
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
    private static final String
            CONF_PATH_PROPERTY = "sslify.configpath",
            CONF_FILE_SUFFIX = ".conf.properties";

    private ConfigProperties() {
        super();
    }

    private static final HashMap<String, ConfigProperties> loaded = new HashMap<String, ConfigProperties>();

    static public ConfigProperties getProperties(final String name) throws ConfigLoadingException {
        InputStream inputStream = null;
        if (loaded.containsKey(name)) {
            return loaded.get(name);
        } else {
            final ConfigProperties props = new ConfigProperties();
            final String fullName = name + CONF_FILE_SUFFIX;

            try {
                try {
                    inputStream = getInputStream(fullName);
                    props.load(inputStream);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    loaded.put(name, props);
                }
            } catch (IOException e) {
                throw new ConfigLoadingException(fullName);
            }
            return props;
        }
    }

    static private InputStream getInputStream(final String name) throws FileNotFoundException {
        final String confPath = System.getProperty(CONF_PATH_PROPERTY);
        final InputStream stream;
        if (confPath == null) {
            stream = ConfigProperties.class.getClassLoader().getResourceAsStream(name);
        } else {
            stream = new FileInputStream(new File(confPath, name));
        }
        return stream;
    }
}
