package sslify;

import org.bouncycastle.openssl.PEMWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.X509Certificate;

class X509Certificates {
    static String toPEM(final X509Certificate cert) throws IOException {
        StringWriter stringWriter = null;
        PEMWriter pemWriter = null;
        String result = null;

        try {
            stringWriter = new StringWriter();
            pemWriter = new PEMWriter(stringWriter);
            pemWriter.writeObject(cert);
            stringWriter.flush();
            result = stringWriter.toString();
        } finally {
            if (pemWriter != null) {
                pemWriter.close();
            }
            if (stringWriter != null) {
                stringWriter.close();
            }
            return result;
        }
    }
}
