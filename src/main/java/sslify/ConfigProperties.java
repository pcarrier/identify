package sslify;

import java.util.Properties;

public class ConfigProperties extends Properties {
    public static class ConfigLoadingException extends RuntimeException {
    }

    public static enum Domains {LDAP, REPOSITORY, X509, SERVER}
}
