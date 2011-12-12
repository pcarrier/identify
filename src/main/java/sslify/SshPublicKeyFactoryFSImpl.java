package sslify;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Formatter;

@Singleton
public class SshPublicKeyFactoryFSImpl implements SshPublicKeyFactory {
    private static final String PROP_PATH = "repo.keypath";
    private static final String CHARSET = "UTF-8";
    private final ConfigProperties configProperties;
    private Cache cache;

    String getSshPublicKeyText(@NonNull final String user)
            throws SshPublicKey.SshPublicKeyLoadingException {
        final Formatter formatter = new Formatter();
        final String path = formatter.format(configProperties.getProperty(PROP_PATH), user).toString();
        final File keyFile = new File(path);
        try {
            return Files.readFirstLine(keyFile, Charset.forName(CHARSET));
        } catch (IOException e) {
            throw new SshPublicKey.SshPublicKeyLoadingException(e);
        }
    }

    @NotNull
    public SshPublicKey get(@NonNull final String user)
            throws ConfigProperties.ConfigLoadingException, SshPublicKey.SshPublicKeyLoadingException {
        Element cached = cache.get(user);
        if (cached != null) {
            return (SshPublicKey) cached.getValue();
        }
        SshPublicKey grabbed = new SshPublicKey(getSshPublicKeyText(user));
        cache.put(new Element(user, grabbed));
        return grabbed;
    }

    @Inject
    SshPublicKeyFactoryFSImpl(ConfigPropertiesFactory configPropertiesFactory, CacheFactory cacheFactory)
            throws FileNotFoundException {
        this.configProperties = configPropertiesFactory.get(ConfigProperties.Domain.REPOSITORY);
        this.cache = cacheFactory.getCache(CacheFactory.Domain.REPOSITORY);
    }
}
