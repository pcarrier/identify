package sslify.models;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Delegate;
import org.bouncycastle.openssl.PEMWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringWriter;


@Data
@AllArgsConstructor
public class X509Certificate {
    @Delegate
    private java.security.cert.X509Certificate cert;

    @NotNull
    public String toPEM() throws IOException {
        StringWriter stringWriter = null;
        PEMWriter pemWriter = null;
        String result = null;

        try {
            stringWriter = new StringWriter();
            pemWriter = new PEMWriter(stringWriter);
            pemWriter.writeObject(this);
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

    @Inject
    X509Certificate(@Assisted String user) {}
}
