package sslify;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import javax.naming.NamingException;
import java.security.GeneralSecurityException;

public interface X509CertificateFactory {
    @NotNull
    X509Certificate get(@NonNull String user) throws GeneralSecurityException, NamingException;
}
