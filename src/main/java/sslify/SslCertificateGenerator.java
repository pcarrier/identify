package sslify;

import com.eaio.uuid.UUID;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

import javax.naming.NamingException;
import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;

public class SslCertificateGenerator {
    static SslCertificateGenerator singleton = null;
    private static final String PROPS_HOURS_BEFORE = "hours.before";
    private static final String PROPS_HOURS_AFTER = "hours.after";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    private static final String GROUP_PREFIX = "group:";

    private KeyPair keyPair;
    private X509Certificate caCert;


    static SslCertificateGenerator getInstance() {
        if (singleton == null) {
            singleton = new SslCertificateGenerator();
        }
        return singleton;
    }

    X509Certificate createCert(String user) throws IOException, CertificateParsingException, InvalidKeySpecException, NoSuchAlgorithmException, NamingException, SignatureException, InvalidKeyException, NoSuchProviderException, CertificateEncodingException {
        final UUID uuid = new UUID();
        final X509V3CertificateGenerator generator = new X509V3CertificateGenerator();

        final ConfigProperties props = ConfigProperties.getProperties(ConfigProperties.X509);

        final CertInfo infos = CertInfo.fromLDAP(user);
        final SshPublicKey sshKey = SshPublicKey.fromRepo(user);

        final Calendar calendar = Calendar.getInstance();
        final int hoursBefore = Integer.parseInt(props.getProperty(PROPS_HOURS_BEFORE));
        final int hoursAfter = Integer.parseInt(props.getProperty(PROPS_HOURS_AFTER));

        generator.setSubjectDN(new X500Principal("CN=" + infos.getCn()));

        calendar.add(Calendar.HOUR, -hoursBefore);
        generator.setNotBefore(calendar.getTime());

        calendar.add(Calendar.HOUR, hoursBefore + hoursAfter);
        generator.setNotAfter(calendar.getTime());

        generator.setSerialNumber(new BigInteger(new Long(uuid.getTime()).toString()));

        generator.addExtension(X509Extensions.AuthorityKeyIdentifier, false,
                new AuthorityKeyIdentifierStructure(caCert));

        generator.addExtension(X509Extensions.SubjectKeyIdentifier, false,
                new SubjectKeyIdentifierStructure(keyPair.getPublic()));

        generator.addExtension(X509Extensions.SubjectAlternativeName, false,
                new GeneralNames(new GeneralName(GeneralName.rfc822Name, infos.getMail())));

        for (final String group: infos.getGroups()) {
            generator.addExtension(X509Extensions.SubjectAlternativeName, false,
                    new GeneralNames(new GeneralName(GeneralName.otherName, GROUP_PREFIX +group)));
        }

        generator.addExtension(X509Extensions.IssuingDistributionPoint, false,
                new DEROctetString(uuid.toString().getBytes()));

        generator.setIssuerDN(caCert.getSubjectX500Principal());
        generator.setPublicKey(keyPair.getPublic());
        generator.setSignatureAlgorithm(SIGNATURE_ALGORITHM);

        return generator.generate(keyPair.getPrivate(), "BC");
    }

    private SslCertificateGenerator() {
    }
}
