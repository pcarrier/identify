package identify;

import lombok.Data;
import org.bouncycastle.openssl.PEMWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringWriter;


@Data
public class X509Certificate {
    private final java.security.cert.X509Certificate cert;

    @NotNull
    public String toPEM() throws IOException {
        StringWriter stringWriter = null;
        PEMWriter pemWriter = null;
        String result = null;
        try {
            stringWriter = new StringWriter();
            pemWriter = new PEMWriter(stringWriter);
            pemWriter.writeObject(this.cert);
            pemWriter.flush();
            stringWriter.flush();
            result = stringWriter.toString();
        } finally {
            if (pemWriter != null)
                pemWriter.close();
            if (stringWriter != null)
                stringWriter.close();
        }
        return result;
    }
}
