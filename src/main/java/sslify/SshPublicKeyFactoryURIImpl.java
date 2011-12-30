package sslify;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Formatter;

@Slf4j
@Singleton
public class SshPublicKeyFactoryURIImpl implements SshPublicKeyFactory {
    private static final String PROP_PATH = "repo.keypath";
    private static final String CHARSET = "UTF-8";
    private final ConfigProperties configProperties;
    private Cache cache;

    String getSshPublicKeyText(@NonNull final String user)
            throws SshPublicKey.SshPublicKeyLoadingException {
        final Formatter formatter = new Formatter();
        final String path = formatter.format(configProperties.getProperty(PROP_PATH), user).toString();
        String line = null;

        BufferedReader reader = null;
        try {
            final URL keyURL = new URL(path);
            reader = new BufferedReader(new InputStreamReader(keyURL.openStream()));
            line = reader.readLine();
        } catch (IOException e) {
            throw new SshPublicKey.SshPublicKeyLoadingException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    SshPublicKeyFactoryURIImpl.log.error(Arrays.toString(e.getStackTrace()));
                }
            }
            return line;
        }
    }

    /* Optimisticly non-synchronized */
    @NotNull
    public SshPublicKey get(@NonNull final String user)
            throws ConfigProperties.ConfigLoadingException, SshPublicKey.SshPublicKeyLoadingException {
        Element element = cache.get(user);
        if (element != null) {
            SshPublicKey cached = (SshPublicKey) element.getValue();
            if (cached == null) {
                SshPublicKeyFactoryURIImpl.log.debug("cached failure:'{}'", user);
                throw new CachedFailureException();
            } else {
                SshPublicKeyFactoryURIImpl.log.debug("cached user:'{}'", user);
                return cached;
            }
        }

        SshPublicKeyFactoryURIImpl.log.debug("uncached user:'{}'", user);
        SshPublicKey grabbed = null;
        try {
            grabbed = new SshPublicKey(getSshPublicKeyText(user));
        } finally {
            cache.put(new Element(user, grabbed));
        }
        return grabbed;
    }

    @Inject
    SshPublicKeyFactoryURIImpl(ConfigPropertiesFactory configPropertiesFactory, CacheFactory cacheFactory)
            throws FileNotFoundException, ConfigProperties.ConfigLoadingException {
        this.configProperties = configPropertiesFactory.get(ConfigProperties.Domain.REPOSITORY);
        this.cache = cacheFactory.getCache(CacheFactory.Domain.REPOSITORY);
    }
}
