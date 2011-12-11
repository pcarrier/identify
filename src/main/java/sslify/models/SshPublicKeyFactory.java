package sslify.models;

import com.google.inject.assistedinject.Assisted;
import org.jetbrains.annotations.NotNull;

public interface SshPublicKeyFactory {
    @NotNull
    SshPublicKey get(@Assisted String user) throws SshPublicKey.SshPublicKeyLoadingException;
}
