package sslify;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class ConfigProperties extends Properties {
    public static String LDAP = "ldap";
    public static String REPO = "repo";

    private ConfigProperties() {
        super();
    }

    ;

    public static HashMap<String, ConfigProperties> loaded = new HashMap<String, ConfigProperties>();

    static public ConfigProperties getProperties(String name) throws IOException {
        if (loaded.containsKey(name)) {
            return loaded.get(name);
        } else {
            ConfigProperties props = new ConfigProperties();
            final String fullName = name + ".conf.properties";
            final InputStream inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream(fullName);
            if (inputStream == null)
                throw new IOException("Failed reading " + fullName);
            props.load(inputStream);
            loaded.put(name, props);
            return props;
        }
    }
}
