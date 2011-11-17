package sslify;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Formatter;

public class FSDataSource {
    private static FSDataSource singleton = null;
    private static final String PROP_PATH = "repo.keypath";


    String getSshPublicKeyText(final String name) throws IOException {
        final Formatter formatter = new Formatter();
        final ConfigProperties props = ConfigProperties.getProperties(ConfigProperties.REPO);
        final String path = formatter.format(props.getProperty(PROP_PATH), name).toString();
        final File keyFile = new File(path);
        return Files.readFirstLine(keyFile, Charset.forName("UTF-8"));
    }

    SshPublicKey getSshPublicKey(final String name) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        return new SshPublicKey(getSshPublicKeyText(name));
    }

    static FSDataSource getInstance() {
        if (singleton == null) {
            singleton = new FSDataSource();
        }
        return singleton;
    }

    private FSDataSource() {
        super();
    }
}
