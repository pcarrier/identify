package identify;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Formatter;

@Slf4j
@Singleton
public class SshPublicKeyFactoryImpl implements SshPublicKeyFactory {
    private static final String PROP_PATH = "repo.keypath";
    private final ConfigProperties configProperties;

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
        } catch (MalformedURLException e) {
            throw new SshPublicKey.SshPublicKeyLoadingException(e);
        }
        catch (IOException e) {
            throw new SshPublicKey.SshPublicKeyLoadingException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    SshPublicKeyFactoryImpl.log.error("{} {}", e.toString(), Arrays.toString(e.getStackTrace()));
                }
            }
        }
        return line;
    }

    @NotNull
    public SshPublicKey get(@NonNull final String user)
            throws ConfigProperties.ConfigLoadingException, SshPublicKey.SshPublicKeyLoadingException {
        return new SshPublicKey(getSshPublicKeyText(user));
    }

    @Inject
    SshPublicKeyFactoryImpl(ConfigPropertiesFactory configPropertiesFactory)
            throws FileNotFoundException, ConfigProperties.ConfigLoadingException {
        this.configProperties = configPropertiesFactory.get(ConfigProperties.Domain.REPOSITORY);
    }
}
