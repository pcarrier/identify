package sslify;

import com.google.inject.Singleton;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.EnumMap;
import java.util.Map;

@Singleton
public class ConfigPropertiesFactoryImpl implements ConfigPropertiesFactory {
    private static final String CONF_FILE_SUFFIX = ".conf.properties";

    private static Map<ConfigProperties.Domain, String> DomainMapping =
            new EnumMap<ConfigProperties.Domain, String>(ConfigProperties.Domain.class);

    static {
        DomainMapping.put(ConfigProperties.Domain.LDAP, "ldap");
        DomainMapping.put(ConfigProperties.Domain.REPOSITORY, "repo");
        DomainMapping.put(ConfigProperties.Domain.X509, "x509");
        DomainMapping.put(ConfigProperties.Domain.HTTP_SERVER, "http.server");
    }

    private static final Map<ConfigProperties.Domain, ConfigProperties> loaded =
            new EnumMap<ConfigProperties.Domain, ConfigProperties>(ConfigProperties.Domain.class);

    @NotNull
    @Override
    public synchronized ConfigProperties get(@NonNull ConfigProperties.Domain domain)
            throws ConfigProperties.ConfigLoadingException {
        if (loaded.containsKey(domain))
            return loaded.get(domain);

        final ConfigProperties props = new ConfigProperties();
        final String fullName = DomainMapping.get(domain) + CONF_FILE_SUFFIX;
        InputStream inputStream = null;

        try {
            inputStream = ConfigFileLocator.getInputStream(fullName);
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

}
