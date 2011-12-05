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
    private static final String CONFIGPATH = "sslify.configpath";

    private ConfigProperties() {
        super();
    }

    private static final HashMap<String, ConfigProperties> loaded = new HashMap<String, ConfigProperties>();

    static public ConfigProperties getProperties(final String name) throws ConfigLoadingException {
        if (loaded.containsKey(name)) {
            return loaded.get(name);
        } else {
            final ConfigProperties props = new ConfigProperties();
            final String fullName = name + ".conf.properties";

            try {
                InputStream inputStream = getInputStream(fullName);
                props.load(inputStream);
                inputStream.close();
            } catch (IOException e) {
                throw new ConfigLoadingException(fullName);
            }
            finally {
                loaded.put(name, props);
            }
            return props;
        }
    }

    static private InputStream getInputStream(String name) throws FileNotFoundException {
        final String confPath = System.getProperty(CONFIGPATH);
        InputStream stream;
        if(confPath == null) {
            stream = ConfigProperties.class.getClassLoader().getResourceAsStream(name);
        } else {
            stream = new FileInputStream(new File(confPath, name));
        }
        return stream;
    }
}
