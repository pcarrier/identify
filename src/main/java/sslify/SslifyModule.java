package sslify;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import sslify.models.*;

public class SslifyModule extends AbstractModule {
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(ConfigProperties.class, ConfigProperties.class)
                .build(ConfigPropertiesFactory.class));
        install(new FactoryModuleBuilder()
                .implement(CertInfo.class, CertInfo.class)
                .build(CertInfoFactory.class));
        install(new FactoryModuleBuilder()
                .implement(SshPublicKey.class, SshPublicKey.class)
                .build(SshPublicKeyFactory.class));
        install(new FactoryModuleBuilder()
                .implement(X509Certificate.class, X509Certificate.class)
                .build(X509CertificateFactory.class));
    }
}
