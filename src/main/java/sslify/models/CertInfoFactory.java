package sslify.models;

import com.google.inject.assistedinject.Assisted;
import org.jetbrains.annotations.NotNull;

import javax.naming.NamingException;

public interface CertInfoFactory {
    @NotNull
    CertInfo get(@Assisted String user) throws NamingException;
}
