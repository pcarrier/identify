package identify;

import com.google.inject.AbstractModule;

public class IdentifyModule extends AbstractModule {
    protected void configure() {
        bind(ConfigPropertiesFactory.class)
                .to(ConfigPropertiesFactoryImpl.class);
        bind(UserInfoFactory.class)
                .to(UserInfoFactoryImpl.class);
        bind(SshPublicKeyFactory.class)
                .to(SshPublicKeyFactoryImpl.class);
        bind(X509CertificateFactory.class)
                .to(X509CertificateFactoryImpl.class);
        bind(TreeGeneratorFactory.class)
                .to(TreeGeneratorFactoryImpl.class);
    }
}
