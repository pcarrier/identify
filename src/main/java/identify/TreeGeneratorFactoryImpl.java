package identify;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TreeGeneratorFactoryImpl implements TreeGeneratorFactory {
    private UserInfoFactory certInfoFactory;
    private X509CertificateFactory x509Factory;

    @Inject
    public TreeGeneratorFactoryImpl(UserInfoFactory certInfoFactory, X509CertificateFactory x509Factory) {
        this.certInfoFactory = certInfoFactory;
        this.x509Factory = x509Factory;
    }

    public TreeGenerator getGenerator(String path) {
        return new TreeGenerator(certInfoFactory, x509Factory, path);
    }
}
