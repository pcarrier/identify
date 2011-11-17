package sslify;

import com.google.common.base.Objects;

import javax.naming.NamingException;
import java.io.IOException;
import java.util.ArrayList;

public class CertInfo {

    private String cn;
    private String uid;
    private String mail;

    public String getCn() {
        return cn;
    }

    public String getUid() {
        return uid;
    }

    public String getMail() {
        return mail;
    }

    public ArrayList<String> getGroups() {
        return groups;
    }

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

    public static CertInfo fromLDAP(final String name) throws NamingException, IOException {
        return LDAPDataSource.getInstance().getInfos(name);
    }
}
