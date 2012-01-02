package sslify;

import org.jetbrains.annotations.NotNull;

public interface SshPublicKeyFactory {
    @NotNull
    SshPublicKey get(String user) throws SshPublicKey.SshPublicKeyLoadingException, ConfigProperties.ConfigLoadingException;


    public static class CachedFailureException extends SshPublicKey.SshPublicKeyLoadingException {
        public CachedFailureException(Exception e) {
            super(e);
        }

        public CachedFailureException() {
        }
    }
}
