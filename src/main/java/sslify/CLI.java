package sslify;

import javax.naming.NamingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

public class CLI {
    private static final String INVALID_SYNTAX_ERROR = "We expect a command followed by usernames as arguments.\n" +
            "Available commands: ldap, sshtext, sshkey, sslcert";

    public static void main(final String[] args) {
        if (args.length < 2) {
            System.err.println(INVALID_SYNTAX_ERROR);
            System.exit(1);
        } else if (args[0].equals("ldap")) {
            loopOverArguments(args, 1, new Runnable() {
                @Override
                public void exec(String name) throws Exception {
                    System.out.println(CertInfo.fromLDAP(name).toString());
                }
            });
        } else if (args[0].equals("sshtext")) {
            loopOverArguments(args, 1, new Runnable() {
                @Override
                public void exec(String name) throws Exception {
                    System.out.println(FSDataSource.getInstance().getSshPublicKeyText(name).toString());
                }
            });
        } else if (args[0].equals("sshkey")) {
            loopOverArguments(args, 1, new Runnable() {
                @Override
                public void exec(String name) throws Exception {
                    System.out.println(FSDataSource.getInstance().getSshPublicKey(name).toString());
                }
            });
        } else if (args[0].equals("sslcert")) {
            loopOverArguments(args, 1, new Runnable() {
                @Override
                public void exec(String name) throws Exception {
                    X509Certificate cert = X509CertificateGenerator.getInstance().createCert(name);
                    System.out.println(X509Certificates.toPEM(cert));
                }
            });
        }
        System.exit(0);
    }

    private static void loopOverArguments(String[] args, int startAt, Runnable runnable) {
        int end = args.length;
        for(int i = startAt; i < end; i++) {
            try {
                runnable.exec(args[i]);
            }
            catch(Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private interface Runnable {
        abstract void exec(String name) throws Exception;
    }
}
