package sslify;

import com.google.inject.AbstractModule;

public class SslifyModule extends AbstractModule {
    protected void configure() {
        bind(ConfigPropertiesFactory.class)
                .to(ConfigPropertiesFactoryImpl.class);
        bind(CertInfoFactory.class)
                .to(CertInfoFactoryLDAPImpl.class);
        bind(SshPublicKeyFactory.class)
                .to(SshPublicKeyFactoryFSImpl.class);
        bind(X509CertificateFactory.class)
                .to(X509CertificateFactoryGeneratorImpl.class);
    }
}
