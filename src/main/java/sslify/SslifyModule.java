package sslify;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import sslify.factories.ConfigSource;
import sslify.factories.FSSource;
import sslify.factories.LDAPSource;
import sslify.factories.X509CertificateGenerator;
import sslify.models.CertInfo;
import sslify.models.ConfigProperties;
import sslify.models.SshPublicKey;
import sslify.models.X509Certificate;

public class SslifyModule extends AbstractModule {
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(ConfigProperties.class, ConfigProperties.class)
                .build(ConfigSource.class));
        install(new FactoryModuleBuilder()
                .implement(CertInfo.class, CertInfo.class)
                .build(LDAPSource.class));
        install(new FactoryModuleBuilder()
                .implement(SshPublicKey.class, SshPublicKey.class)
                .build(FSSource.class));
        install(new FactoryModuleBuilder()
                .implement(X509Certificate.class, X509Certificate.class)
                .build(X509CertificateGenerator.class));
    }
}
