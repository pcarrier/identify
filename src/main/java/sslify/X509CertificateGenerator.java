package sslify;

import com.eaio.uuid.UUID;
import com.google.common.base.Joiner;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

import javax.naming.NamingException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

public class X509CertificateGenerator {
    private static X509CertificateGenerator singleton = null;

    private static final String
            PROPS_HOURS_BEFORE = "hours.before",
            PROPS_HOURS_AFTER = "hours.after",
            SIGNATURE_ALGORITHM = "SHA1withRSA",
            CA_CERT_PATH = "ca.cert.path",
            CA_KEY_PATH = "ca.key.path";
            // GROUP_PREFIX = "group:";

    /* TODO: initialize */
    private ConfigProperties props;
    private X509Certificate caCert;
    private PrivateKey caPrivateKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public X509CertificateGenerator() throws IOException {
        props = ConfigProperties.getProperties(ConfigProperties.X509);
        caPrivateKey = readCAPrivateKey();
        caCert = readCACert();
    }

    private PrivateKey readCAPrivateKey() throws IOException {
        final File caPrivateKeyFile = new File(props.getProperty(CA_KEY_PATH));
        final FileReader caPrivateKeyFileReader = new FileReader(caPrivateKeyFile);
        final PEMReader caPrivateKeyReader = new PEMReader(caPrivateKeyFileReader, EmptyPasswordFinder.getInstance());

        KeyPair keypair = (KeyPair) caPrivateKeyReader.readObject();
        PrivateKey pkey = keypair.getPrivate();

        caPrivateKeyReader.close();
        caPrivateKeyFileReader.close();
        return pkey;
    }

    private X509Certificate readCACert() throws IOException {
        final File caCertFile = new File(props.getProperty(CA_CERT_PATH));
        final FileReader caCertFileReader = new FileReader(caCertFile);
        final PEMReader caCertReader = new PEMReader(caCertFileReader, EmptyPasswordFinder.getInstance());
        X509Certificate cert = (X509Certificate) caCertReader.readObject();

        caCertReader.close();
        caCertFileReader.close();

        return cert;
    }

    public java.security.cert.X509Certificate createCert(final String user) throws IOException, NamingException, InvalidKeySpecException, NoSuchAlgorithmException, CertificateException, SignatureException, InvalidKeyException, NoSuchProviderException {
        final UUID uuid = new UUID();
        final X509V3CertificateGenerator generator = new X509V3CertificateGenerator();

        final CertInfo infos = CertInfo.fromLDAP(user);
        final SshPublicKey sshKey = SshPublicKey.fromRepo(user);

        final Calendar calendar = Calendar.getInstance();
        final int hoursBefore = Integer.parseInt(props.getProperty(PROPS_HOURS_BEFORE));
        final int hoursAfter = Integer.parseInt(props.getProperty(PROPS_HOURS_AFTER));

        final Vector<DERObjectIdentifier> attrsVector = new Vector<DERObjectIdentifier>();
        final Hashtable<DERObjectIdentifier, String> attrsHash = new Hashtable<DERObjectIdentifier, String>();

        attrsHash.put(X509Principal.CN, infos.getCn());
        attrsVector.add(X509Principal.CN);

        attrsHash.put(X509Principal.UID, infos.getUid());
        attrsVector.add(X509Principal.UID);

        attrsHash.put(X509Principal.EmailAddress, infos.getMail());
        attrsVector.add(X509Principal.EmailAddress);

        attrsHash.put(X509Principal.OU, Joiner.on('+').join(infos.getGroups()));
        attrsVector.add(X509Principal.OU);

        generator.setSubjectDN(new X509Principal(attrsVector, attrsHash));

        calendar.add(Calendar.HOUR, -hoursBefore);
        generator.setNotBefore(calendar.getTime());

        calendar.add(Calendar.HOUR, hoursBefore + hoursAfter);
        generator.setNotAfter(calendar.getTime());

        // Reuse the UUID time as a SN
        generator.setSerialNumber(new BigInteger(new Long(uuid.getTime()).toString()).abs());

        generator.addExtension(X509Extensions.AuthorityKeyIdentifier, false,
                new AuthorityKeyIdentifierStructure(caCert));

        generator.addExtension(X509Extensions.SubjectKeyIdentifier, false,
                new SubjectKeyIdentifierStructure(sshKey.getKey()));

        // Store the UUID
        generator.addExtension(X509Extensions.IssuingDistributionPoint, false,
                uuid.toString().getBytes());

        // Not a CA
        generator.addExtension(X509Extensions.BasicConstraints, true,
                new BasicConstraints(false));

        generator.setIssuerDN(caCert.getSubjectX500Principal());
        generator.setPublicKey(sshKey.getKey());
        generator.setSignatureAlgorithm(SIGNATURE_ALGORITHM);

        final java.security.cert.X509Certificate cert = generator.generate(caPrivateKey, "BC");

        cert.checkValidity();
        cert.verify(caCert.getPublicKey());

        return cert;
    }

    private static class EmptyPasswordFinder implements PasswordFinder {
        private static EmptyPasswordFinder singleton = new EmptyPasswordFinder();

        public char[] getPassword() {
            return new char[0];
        }

        public static EmptyPasswordFinder getInstance() {
            return singleton;
        }
    }

    public static X509CertificateGenerator getInstance() throws IOException {
        if (singleton == null) {
            singleton = new X509CertificateGenerator();
        }
        return singleton;
    }
}
