package sslify;

import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.openssl.PEMWriter;

public class X509Certificates {
    static String toPEM(X509Certificate cert) throws IOException {
        StringWriter stringWriter = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(stringWriter);
        pemWriter.writeObject(cert);
        pemWriter.close();
        stringWriter.flush();
        String result = stringWriter.toString();
        stringWriter.close();
        return result;
    }
}
