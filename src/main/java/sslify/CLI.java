package sslify;

import java.security.cert.X509Certificate;

public class CLI {
    private static final String INVALID_SYNTAX_ERROR = "We expect a command followed by usernames as arguments.\n" +
            "Available commands: ldap, sshtext, sshkey, sslcert";
    private static final String
            LDAP_COMMAND = "ldap",
            SSH_TEXT_COMMAND = "sshtext",
            SSH_KEY_COMMAND = "sshkey",
            SSL_CERT_COMMAND = "sslcert";

    public static void main(final String[] args) {
        if (args.length < 2) {
            System.err.println(INVALID_SYNTAX_ERROR);
            System.exit(1);
        } else if (args[0].equals(LDAP_COMMAND)) {
            loopOverArguments(args, new Runnable() {
                @Override
                public void exec(final String name) throws Exception {
                    System.out.println(CertInfo.fromLDAP(name).toString());
                }
            });
        } else if (args[0].equals(SSH_TEXT_COMMAND)) {
            loopOverArguments(args, new Runnable() {
                @Override
                public void exec(final String name) throws Exception {
                    System.out.println(FSDataSource.getInstance().getSshPublicKeyText(name).toString());
                }
            });
        } else if (args[0].equals(SSH_KEY_COMMAND)) {
            loopOverArguments(args, new Runnable() {
                @Override
                public void exec(final String name) throws Exception {
                    System.out.println(FSDataSource.getInstance().getSshPublicKey(name).toString());
                }
            });
        } else if (args[0].equals(SSL_CERT_COMMAND)) {
            loopOverArguments(args, new Runnable() {
                @Override
                public void exec(final String name) throws Exception {
                    final X509Certificate cert = X509CertificateGenerator.getInstance().createCert(name);
                    System.out.println(X509Certificates.toPEM(cert));
                }
            });
        }
        System.exit(0);
    }

    private static void loopOverArguments(final String[] args, final Runnable runnable) {
        final int end = args.length;
        for (int i = 1; i < end; i++) {
            try {
                runnable.exec(args[i]);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private interface Runnable {
        abstract void exec(String name) throws Exception;
    }
}
