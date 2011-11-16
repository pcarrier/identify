package sslify;

import com.google.common.base.Objects;

import javax.naming.NamingException;
import java.io.IOException;
import java.util.ArrayList;

public class CertInfo {

    private String cn;
    private String uid;
    private String mail;
    private ArrayList<String> groups = new ArrayList<String>();

    public String toString() {
        return Objects.toStringHelper(this).add("cn", cn).add("uid", uid).add("mail", mail).add("groups", groups).toString();
    }

    public CertInfo(String cn, String uid, String mail, ArrayList<String> groups) {
        this.cn = cn;
        this.uid = uid;
        this.mail = mail;
        this.groups = groups;
    }

    public static CertInfo fromLDAP(String name) throws NamingException, IOException {
        return LDAPDataSource.getInstance().getInfos(name);
    }
}
