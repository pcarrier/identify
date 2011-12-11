package sslify.models;

import com.google.inject.assistedinject.Assisted;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import javax.naming.NamingException;
import java.security.GeneralSecurityException;

public interface X509CertificateFactory {
    @NotNull
    X509Certificate get(@NonNull @Assisted String user) throws GeneralSecurityException, NamingException;
}
