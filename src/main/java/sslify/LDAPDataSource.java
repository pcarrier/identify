package sslify;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Hashtable;

class LDAPDataSource extends InitialDirContext {
    static class MissingDetailsException extends NamingException {
    }

    static class MissingUserException extends NamingException {
    }

    static class TooManyUsersException extends NamingException {
    }

    static LDAPDataSource singleton = null;

    static String PROP_BASE_DN_USER = "queries.basedn.user"; //NON-NLS
    static String PROP_QUERIES_USER = "queries.filter.user"; //NON-NLS
    static String PROP_BASE_DN_GROUPS = "queries.basedn.groups"; //NON-NLS
    static String PROP_QUERIES_GROUPS = "queries.filter.groups"; //NON-NLS
    static String[] USER_ATTRIBUTES = {"cn", "uid", "mail"};
    static String[] GROUP_ATTRIBUTES = {"cn"};

    private LDAPDataSource(Hashtable<?, ?> environment) throws NamingException {
        super(environment);
    }

    static LDAPDataSource getInstance() throws NamingException, IOException {
        if (singleton == null) {
            singleton = new LDAPDataSource(ConfigProperties.getProperties(ConfigProperties.LDAP));
        }
        return singleton;
    }

    CertInfo getInfos(String name) throws NamingException, IOException {
        ConfigProperties props = ConfigProperties.getProperties(ConfigProperties.LDAP);
        Formatter formatter = new Formatter();
        
        String cn, uid, mail;
        ArrayList<String> groups = new ArrayList<String>();

        final SearchControls user_controls = new SearchControls();
        user_controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        user_controls.setReturningAttributes(USER_ATTRIBUTES);

        final SearchControls groups_controls = new SearchControls();
        groups_controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        groups_controls.setReturningAttributes(GROUP_ATTRIBUTES);

        String user_query = formatter.format(props.getProperty(PROP_QUERIES_USER), name).toString();
        String groups_query = formatter.format(props.getProperty(PROP_QUERIES_GROUPS), name).toString();

        final NamingEnumeration<SearchResult> user_results = this.search(props.getProperty(PROP_BASE_DN_USER), user_query, user_controls);
        if (!user_results.hasMore())
            throw new MissingUserException();
        Attributes attributes = user_results.next().getAttributes();
        if (user_results.hasMore())
            throw new TooManyUsersException();

        try {
            cn = attributes.get("cn").get().toString();
            uid = attributes.get("uid").get().toString();
            mail = attributes.get("mail").get().toString();
        } catch (NullPointerException e) {
            throw new MissingDetailsException();
        }

        final NamingEnumeration<SearchResult> groups_results = this.search(props.getProperty(PROP_BASE_DN_GROUPS), groups_query, groups_controls);
        while (groups_results.hasMore()) {
            groups.add(groups_results.next().getAttributes().get("cn").get().toString());
        }
        
        return new CertInfo(cn, uid, mail, groups);
    }
}
