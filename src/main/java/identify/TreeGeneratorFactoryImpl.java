package identify;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TreeGeneratorFactoryImpl implements TreeGeneratorFactory {
    private final ConfigPropertiesFactory configPropertiesFactory;
    private final UserInfoFactory certInfoFactory;
    private final X509CertificateFactory x509Factory;

    @Inject
    public TreeGeneratorFactoryImpl(final ConfigPropertiesFactory configPropertiesFactory,
                                    final UserInfoFactory certInfoFactory,
                                    final X509CertificateFactory x509Factory) {
        this.configPropertiesFactory = configPropertiesFactory;
        this.certInfoFactory = certInfoFactory;
        this.x509Factory = x509Factory;
    }

    public final TreeGenerator getGenerator(final String path) throws ConfigProperties.ConfigLoadingException {
        return new TreeGenerator(configPropertiesFactory, certInfoFactory, x509Factory, path);
    }
}
