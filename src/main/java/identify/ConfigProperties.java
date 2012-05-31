package identify;

import java.util.Properties;

public class ConfigProperties extends Properties {
    public static class ConfigLoadingException extends Exception {
        public ConfigLoadingException(Exception e) {
            super(e);
        }
    }
    public static enum Domain {LDAP, REPOSITORY, X509, TREE}
}
