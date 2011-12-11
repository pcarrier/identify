package sslify.models;

import org.jetbrains.annotations.NotNull;

public interface ConfigPropertiesFactory {
    @NotNull
    ConfigProperties get(ConfigProperties.Domains domain) throws ConfigProperties.ConfigLoadingException;
}
