package sslify;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestCLI {
    private static final String INVALID_SYNTAX_ERROR = "We expect a command followed by usernames as arguments.\n" +
            "Available commands: ldap, sshtext, sshkey, sslcert";
    private static final String LDAP_COMMAND = "ldap";
    private static final String SSH_KEY_COMMAND = "sshkey";
    private static final String SSL_CERT_COMMAND = "sslcert";

    public static void main(final String[] args) {
        final Injector injector = Guice.createInjector(new SslifyModule());
        if (args.length < 2) {
            System.err.println(INVALID_SYNTAX_ERROR);
            System.exit(1);
        } else if (args[0].equals(LDAP_COMMAND)) {
            loopOverArguments(args, new Runnable() {
                public void exec(final String name) throws Exception {
                    System.out.println(injector.getInstance(CertInfoFactory.class).get(name).toString());
                }
            });
        } else if (args[0].equals(SSH_KEY_COMMAND)) {
            loopOverArguments(args, new Runnable() {
                public void exec(final String name) throws Exception {
                    System.out.println(injector.getInstance(SshPublicKeyFactory.class).get(name).toString());
                }
            });
        } else if (args[0].equals(SSL_CERT_COMMAND)) {
            loopOverArguments(args, new Runnable() {
                public void exec(final String name) throws Exception {
                    System.out.println(injector.getInstance(X509CertificateFactory.class).get(name).toPEM());
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
