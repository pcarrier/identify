package sslify;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.Data;
import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

@Data
public class SshPublicKey implements Serializable {
    private static final String NO_COMMENT_AVAILABLE = "unavailable";
    private static final String SSH_RSA_PREFIX = "ssh-rsa";

    enum Type {RSA}

    private final Type type;
    private final PublicKey key;
    private final String comment;
    private final String fingerprint;

    private static BigInteger readMPInt(final ByteArrayDataInput source) {
        final int length = source.readInt();
        final byte[] dest = new byte[length];
        source.readFully(dest, 0, length);
        return new BigInteger(dest);
    }

    private static String readString(final ByteArrayDataInput source) {
        final int length = source.readInt();
        final byte[] dest = new byte[length];
        source.readFully(dest, 0, length);
        return new String(dest);
    }

    public static class SshPublicKeyLoadingException extends RuntimeException {
        public SshPublicKeyLoadingException(Exception e) {
            super(e);
        }
    }

    public SshPublicKey(final String description) throws SshPublicKeyLoadingException {
        final String[] parts = Iterables.toArray(Splitter.on(CharMatcher.JAVA_WHITESPACE).split(description), String.class);
        final String encodedKey;

        if (parts.length == 1) {
            comment = NO_COMMENT_AVAILABLE;
            encodedKey = description;
        } else if (parts.length == 3) {
            comment = parts[2];
            encodedKey = parts[1];
        } else {
            throw new UnreadableKey(UnreadableKey.INVALID_FORMAT);
        }

        final byte[] decodedKey = Base64.decodeBase64(encodedKey);

        final MessageDigest sha1Digester;
        try {
            sha1Digester = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new SshPublicKeyLoadingException(e);
        }
        sha1Digester.update(decodedKey);
        fingerprint = new BigInteger(sha1Digester.digest()).toString(16);

        final ByteArrayDataInput keyInput = ByteStreams.newDataInput(decodedKey);
        final String type_string = readString(keyInput);

        if (!type_string.equals(SSH_RSA_PREFIX)) {
            throw new UnreadableKey(UnreadableKey.INVALID_KEY_TYPE);
        } else {
            type = Type.RSA;
            final BigInteger exp = readMPInt(keyInput);
            final BigInteger mod = readMPInt(keyInput);
            try {
                key = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(mod, exp));
            } catch (InvalidKeySpecException e) {
                throw new SshPublicKeyLoadingException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new SshPublicKeyLoadingException(e);
            }
        }
    }

    public static class UnreadableKey extends IllegalStateException {
        final public static String INVALID_FORMAT = "Invalid format";
        final public static String INVALID_KEY_TYPE = "Invalid key type";

        public UnreadableKey(final String s) {
            super(s);
        }
    }
}
