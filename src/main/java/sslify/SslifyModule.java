package sslify;

import com.google.inject.AbstractModule;
import org.eclipse.jetty.server.Handler;
import sslify.web.X509Servlet;

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
        bind(CacheFactory.class)
                .to(CacheFactoryImpl.class);
    }
}
