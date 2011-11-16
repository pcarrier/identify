package sslify;

public class SslCertificateGenerator {
    static SslCertificateGenerator singleton = null;

    static SslCertificateGenerator getInstance() {
        if (singleton == null) {
            singleton = new SslCertificateGenerator();
        }
        return singleton;
    }

    private SslCertificateGenerator() {
    }
}
