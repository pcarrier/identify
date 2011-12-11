package sslify.models;

import com.google.inject.assistedinject.Assisted;
import org.jetbrains.annotations.NotNull;

public interface ConfigPropertiesFactory {
    @NotNull
    ConfigProperties get(@Assisted ConfigProperties.Domains domain) throws ConfigProperties.ConfigLoadingException;
}
