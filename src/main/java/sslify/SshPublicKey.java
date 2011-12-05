package sslify;

import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class SshPublicKey {
    private static final String NO_COMMENT_AVAILABLE = "unavailable";
    private static final String SSH_RSA_PREFIX = "ssh-rsa";

    public static class UnreadableKey extends IllegalStateException {
        final public static String INVALID_FORMAT = "Invalid format";
        final public static String INVALID_KEY_TYPE = "Invalid key type";

        public UnreadableKey(final String s) {
            super(s);
        }

    }

    enum Type {RSA}

    private Type type;
    private PublicKey key;
    private String comment;
    private String fingerprint;

    public Type getType() {
        return type;
    }

    public PublicKey getKey() {
        return key;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("type", type).add("fingerprint", fingerprint).add("comment", comment).toString();
    }

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

    public SshPublicKey(final String description) throws InvalidKeySpecException, NoSuchAlgorithmException, UnreadableKey {
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

        final MessageDigest sha1Digester = MessageDigest.getInstance("SHA-1");
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
            key = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(mod, exp));
        }
    }

    static SshPublicKey fromRepo(final String name) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        return FSDataSource.getInstance().getSshPublicKey(name);
    }
}
