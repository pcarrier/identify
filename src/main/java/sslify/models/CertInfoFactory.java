package sslify.models;

import org.jetbrains.annotations.NotNull;

import javax.naming.NamingException;

public interface CertInfoFactory {
    @NotNull
    CertInfo get(String user) throws NamingException;
}
