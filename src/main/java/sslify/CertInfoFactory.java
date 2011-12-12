package sslify;

import org.jetbrains.annotations.NotNull;

import javax.naming.NamingException;

public interface CertInfoFactory {
    @NotNull
    CertInfo get(String user) throws NamingException;

    public static class MissingDetailsException extends NamingException {
    }

    public static class MissingUserException extends NamingException {
    }

    public static class TooManyUsersException extends NamingException {
    }

    public static class CachedFailureException extends NamingException {
    }
}
