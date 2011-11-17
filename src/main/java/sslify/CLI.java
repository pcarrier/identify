package sslify;

import javax.naming.NamingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class CLI {
    public static void main(String[] args) throws NamingException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (args.length != 2) {
            System.err.println("We expect a command and a username as arguments.");
            System.exit(1);
        } else if ("ldap".equals(args[0])) {
            System.out.println(CertInfo.fromLDAP(args[1]).toString());
        } else if ("sshtext".equals(args[0])) {
            System.out.println(FSDataSource.getInstance().getSshPublicKeyText(args[1]).toString());
        } else if ("sshkey".equals(args[0])) {
            System.out.println(FSDataSource.getInstance().getSshPublicKey(args[1]).toString());
        }
        System.exit(0);
    }
}
