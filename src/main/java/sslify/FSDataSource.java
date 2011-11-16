package sslify;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Formatter;

public class FSDataSource {
    static String PROP_PATH = "repo.keypath";


    String getSshPublicKeyText(String name) throws IOException {
        Formatter formatter = new Formatter();
        ConfigProperties props = ConfigProperties.getProperties(ConfigProperties.REPO);
        String path = formatter.format(props.getProperty(PROP_PATH), name).toString();
        File keyFile = new File(path);
        return Files.readFirstLine(keyFile, Charset.forName("UTF-8"));
    }
    
    SshPublicKey getSshPublicKey(String name) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        return new SshPublicKey(getSshPublicKeyText(name));
    }

    static FSDataSource singleton = null;

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
