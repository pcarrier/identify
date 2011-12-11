package sslify.models;

import org.jetbrains.annotations.NotNull;

public interface SshPublicKeyFactory {
    @NotNull
    SshPublicKey get(String user) throws SshPublicKey.SshPublicKeyLoadingException;
}
