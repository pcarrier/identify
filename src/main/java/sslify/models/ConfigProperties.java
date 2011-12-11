package sslify.models;

import java.util.Properties;

public class ConfigProperties extends Properties {
    public ConfigProperties() {
        super();
    }

    public static class ConfigLoadingException extends RuntimeException {
    }

    public static enum Domains {LDAP, REPOSITORY, X509}

    ;
}
