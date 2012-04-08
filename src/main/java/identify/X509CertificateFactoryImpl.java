package identify;

import com.eaio.uuid.UUID;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.jetbrains.annotations.NotNull;

import javax.naming.NamingException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

@Singleton
public class X509CertificateFactoryImpl implements X509CertificateFactory, PasswordFinder {
    private static final String
            PROPS_HOURS_BEFORE = "hours.before",
            PROPS_HOURS_AFTER = "hours.after",
            PROPS_CHECK = "check",
            SIGNATURE_ALGORITHM = "SHA1withRSA",
            CA_CERT_PATH = "ca.cert.path",
            CA_KEY_PATH = "ca.key.path";

    private final ConfigProperties props;
    private final SshPublicKeyFactory sshPublicKeyFactory;
    private final String hostname;
    private final boolean checkCert;
    final int hoursBefore;
    final int hoursAfter;
    private java.security.cert.X509Certificate caCert;
    private PrivateKey caPrivateKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Inject
    X509CertificateFactoryImpl(ConfigPropertiesFactory configPropertiesFactory,
                               SshPublicKeyFactory sshPublicKeyFactory)
            throws IOException, ConfigProperties.ConfigLoadingException {
        this.props = configPropertiesFactory.get(ConfigProperties.Domain.X509);
        this.hoursBefore = Integer.parseInt(props.getProperty(PROPS_HOURS_BEFORE));
        this.hoursAfter = Integer.parseInt(props.getProperty(PROPS_HOURS_AFTER));
        this.checkCert = Boolean.parseBoolean(props.getProperty(PROPS_CHECK));

        this.sshPublicKeyFactory = sshPublicKeyFactory;
        this.hostname = InetAddress.getLocalHost().getHostName();
        this.caPrivateKey = readCAPrivateKey();
        this.caCert = readCACert();
    }

    private PrivateKey readCAPrivateKey() throws IOException {
        final File caPrivateKeyFile = new File(props.getProperty(CA_KEY_PATH));
        FileReader caPrivateKeyFileReader = null;
        PEMReader caPrivateKeyReader = null;

        try {
            caPrivateKeyFileReader = new FileReader(caPrivateKeyFile);
            caPrivateKeyReader = new PEMReader(caPrivateKeyFileReader, this);
            final KeyPair keypair = (KeyPair) caPrivateKeyReader.readObject();
            return keypair.getPrivate();
        } finally {
            if (caPrivateKeyReader != null)
                caPrivateKeyReader.close();
            if (caPrivateKeyFileReader != null)
                caPrivateKeyFileReader.close();
        }
    }

    private java.security.cert.X509Certificate readCACert() throws IOException {
        final File caCertFile = new File(props.getProperty(CA_CERT_PATH));
        FileReader caCertFileReader = null;
        PEMReader caCertReader = null;

        try {
            caCertFileReader = new FileReader(caCertFile);
            caCertReader = new PEMReader(caCertFileReader, this);
            return (java.security.cert.X509Certificate) caCertReader.readObject();
        } finally {
            if (caCertReader != null)
                caCertReader.close();
            if (caCertFileReader != null)
                caCertFileReader.close();
        }
    }

    @NotNull
    @Override
    public X509Certificate get(UserInfo infos)
            throws GeneralSecurityException, NamingException, SshPublicKey.SshPublicKeyLoadingException, ConfigProperties.ConfigLoadingException {
        final UUID uuid = new UUID();
        final X509V3CertificateGenerator generator = new X509V3CertificateGenerator();

        final SshPublicKey sshKey = sshPublicKeyFactory.get(infos.getUid());

        final Calendar calendar = Calendar.getInstance();

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
        generator.setSerialNumber(BigInteger.valueOf(uuid.getTime()).abs());

        generator.addExtension(X509Extensions.AuthorityKeyIdentifier, false,
                new AuthorityKeyIdentifierStructure(caCert));

        generator.addExtension(X509Extensions.SubjectKeyIdentifier, false,
                new SubjectKeyIdentifierStructure(sshKey.getKey()));

        StringBuilder hostnameAndUUIDBuilder = new StringBuilder(hostname);
        hostnameAndUUIDBuilder.append(':');
        hostnameAndUUIDBuilder.append(uuid.toString());
        generator.addExtension(X509Extensions.IssuingDistributionPoint, false,
                hostnameAndUUIDBuilder.toString().getBytes());

        // Not a CA
        generator.addExtension(X509Extensions.BasicConstraints, true,
                new BasicConstraints(false));

        generator.setIssuerDN(caCert.getSubjectX500Principal());
        generator.setPublicKey(sshKey.getKey());
        generator.setSignatureAlgorithm(SIGNATURE_ALGORITHM);

        final java.security.cert.X509Certificate cert = generator.generate(caPrivateKey, BouncyCastleProvider.PROVIDER_NAME);

        if (this.checkCert) {
            cert.checkValidity();
            cert.verify(caCert.getPublicKey());
        }

        return new X509Certificate(cert);
    }

    @Override
    public char[] getPassword() {
        return new char[0];
    }
}
