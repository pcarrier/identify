package sslify;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.ArrayList;

public class CLIMain {
    private static final String INVALID_SYNTAX_ERROR = "We expect a command.\n" +
            "  Commands taking 1 or more users as parameters: ldap, sshtext, sshkey, sslcert\n" +
            "  Commands without parameters: server\n";
    private static final String LDAP_COMMAND = "ldap";
    private static final String SSH_KEY_COMMAND = "sshkey";
    private static final String SSL_CERT_COMMAND = "sslcert";
    private static final String HTTP_SERVER_COMMAND = "server";

    private static void showHelp() {
        System.err.println(INVALID_SYNTAX_ERROR);
    }

    public static void main(final String[] args) throws Exception {
        final Injector injector = Guice.createInjector(new SslifyModule());
        if (args.length == 0) {
            showHelp();
            System.exit(1);
        } else if (args[0].equals(HTTP_SERVER_COMMAND)) {
             injector.getInstance(sslify.web.HttpServer.class).run();
        } else if (args[0].equals(LDAP_COMMAND)) {
            forAllArguments(args, new Runnable() {
                public void exec(final String name) throws Exception {
                    System.out.print(injector.getInstance(CertInfoFactory.class).get(name).toString());
                }
            });
        } else if (args[0].equals(SSH_KEY_COMMAND)) {
            forAllArguments(args, new Runnable() {
                public void exec(final String name) throws Exception {
                    System.out.print(injector.getInstance(SshPublicKeyFactory.class).get(name).toString());
                }
            });
        } else if (args[0].equals(SSL_CERT_COMMAND)) {
            forAllArguments(args, new Runnable() {
                public void exec(final String name) throws Exception {
                    X509Certificate cert = injector.getInstance(X509CertificateFactory.class).get(name);
                    System.out.print(cert.toPEM());
                }
            });
        } else {
            showHelp();
            System.exit(1);
        }
    }

    private static void forAllArguments(final String[] args, final Runnable runnable) {
        final int end = args.length;
        final ArrayList<Thread> threads = new ArrayList<Thread>();
        for (int i = 1; i < end; i++) {
            final String arg = args[i];
            Thread t = new Thread(new java.lang.Runnable() {
                @Override
                public void run() {
                    try {
                        runnable.exec(arg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            threads.add(t);
            t.run();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private interface Runnable {
        void exec(String name) throws Exception;
    }
}
