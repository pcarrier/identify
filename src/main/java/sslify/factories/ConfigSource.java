package sslify.factories;

import com.google.inject.Singleton;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import sslify.models.ConfigProperties;
import sslify.models.ConfigPropertiesFactory;

import java.io.*;
import java.util.EnumMap;
import java.util.Map;

@Singleton
public class ConfigSource implements ConfigPropertiesFactory {
    private static final String CONF_PATH_PROPERTY = "sslify.configpath";
    private static final String CONF_FILE_SUFFIX = ".conf.properties";

    private static Map<ConfigProperties.Domains, String> DomainMapping =
            new EnumMap<ConfigProperties.Domains, String>(ConfigProperties.Domains.class);

    static {
        DomainMapping.put(ConfigProperties.Domains.LDAP, "ldap");
        DomainMapping.put(ConfigProperties.Domains.REPOSITORY, "repo");
        DomainMapping.put(ConfigProperties.Domains.X509, "x509");
    }

    private static final Map<ConfigProperties.Domains, ConfigProperties> loaded =
            new EnumMap<ConfigProperties.Domains, ConfigProperties>(ConfigProperties.Domains.class);

    @NotNull
    @Override
    public ConfigProperties get(@NonNull ConfigProperties.Domains domain) throws ConfigProperties.ConfigLoadingException {
        // Return memoized value if available
        if (loaded.containsKey(domain))
            return loaded.get(domain);

        final ConfigProperties props = new ConfigProperties();
        final String fullName = DomainMapping.get(domain) + CONF_FILE_SUFFIX;
        InputStream inputStream = null;

        try {
            inputStream = getInputStream(fullName);
            props.load(inputStream);
            loaded.put(domain, props);
            return props;
        } catch (FileNotFoundException e) {
            throw new ConfigProperties.ConfigLoadingException();
        } catch (IOException e) {
            throw new ConfigProperties.ConfigLoadingException();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
        }
    }

    @NotNull
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
