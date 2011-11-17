package sslify;

import com.eaio.uuid.UUID;
import com.google.common.base.Joiner;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

import javax.naming.NamingException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

public class X509Certificate {
    private static final String
            PROPS_HOURS_BEFORE = "hours.before",
            PROPS_HOURS_AFTER = "hours.after",
            SIGNATURE_ALGORITHM = "SHA1withRSA",
            GROUP_PREFIX = "group:";


    /* TODO: initialize that crap */
    private static java.security.cert.X509Certificate caCert;
    private static PrivateKey caPrivateKey;

    static java.security.cert.X509Certificate createCert(final String user) throws IOException, CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, NamingException, SignatureException, InvalidKeyException, NoSuchProviderException, CertificateEncodingException, CertificateExpiredException, CertificateNotYetValidException {
        final UUID uuid = new UUID();
        final X509V3CertificateGenerator generator = new X509V3CertificateGenerator();

        final ConfigProperties props = ConfigProperties.getProperties(ConfigProperties.X509);

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
        generator.setSerialNumber(new BigInteger(new Long(uuid.getTime()).toString()));

        generator.addExtension(X509Extensions.AuthorityKeyIdentifier, false,
                new AuthorityKeyIdentifierStructure(caCert));

        generator.addExtension(X509Extensions.SubjectKeyIdentifier, false,
                new SubjectKeyIdentifierStructure(sshKey.getKey()));

        // Store the UUID
        generator.addExtension(X509Extensions.IssuingDistributionPoint, false,
                new DEROctetString(uuid.toString().getBytes()));

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

    private X509Certificate() {
        super();
        Security.addProvider(new BouncyCastleProvider());
    }
}
