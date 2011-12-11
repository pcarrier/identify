package sslify.models;

import com.google.inject.Inject;

import java.util.Properties;

public class ConfigProperties extends Properties {
    @Inject
    public ConfigProperties() {
        super();
    }

    public static class ConfigLoadingException extends RuntimeException {
    }

    public static enum Domains {LDAP, REPOSITORY, X509}
}
