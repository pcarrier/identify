package sslify;

import javax.naming.NamingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class CLI {
    private static final String INVALID_SYNTAX_ERROR = "We expect a command and a username as arguments.";

    public static void main(final String[] args) throws NamingException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (args.length != 2) {
            System.err.println(INVALID_SYNTAX_ERROR);
            System.exit(1);
        } else if (args[0].equals("ldap")) {
            System.out.println(CertInfo.fromLDAP(args[1]).toString());
        } else if (args[0].equals("sshtext")) {
            System.out.println(FSDataSource.getInstance().getSshPublicKeyText(args[1]).toString());
        } else if (args[0].equals("sshkey")) {
            System.out.println(FSDataSource.getInstance().getSshPublicKey(args[1]).toString());
        }
        System.exit(0);
    }
}
