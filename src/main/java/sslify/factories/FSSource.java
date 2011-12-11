package sslify.factories;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import sslify.models.ConfigProperties;
import sslify.models.ConfigPropertiesFactory;
import sslify.models.SshPublicKey;
import sslify.models.SshPublicKeyFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Formatter;

@Singleton
public class FSSource implements SshPublicKeyFactory {
    private static final String PROP_PATH = "repo.keypath";
    private static final String CHARSET = "UTF-8";
    private final ConfigProperties configProperties;

    String getSshPublicKeyText(final String user)
            throws SshPublicKey.SshPublicKeyLoadingException {
        final Formatter formatter = new Formatter();
        final String path = formatter.format(configProperties.getProperty(PROP_PATH), user).toString();
        final File keyFile = new File(path);
        try {
            return Files.readFirstLine(keyFile, Charset.forName(CHARSET));
        } catch (IOException e) {
            throw new SshPublicKey.SshPublicKeyLoadingException();
        }
    }

    @NotNull
    @Inject
    public SshPublicKey get(@NonNull String user)
            throws ConfigProperties.ConfigLoadingException, SshPublicKey.SshPublicKeyLoadingException {
        return SshPublicKey.fromDescription(getSshPublicKeyText(user));
    }
    
    @Inject
    FSSource(ConfigPropertiesFactory configPropertiesFactory) {
        this.configProperties = configPropertiesFactory.get(ConfigProperties.Domains.REPOSITORY);
    }
}
