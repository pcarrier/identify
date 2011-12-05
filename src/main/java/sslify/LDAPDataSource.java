package sslify;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Hashtable;

class LDAPDataSource extends InitialDirContext {
    private static class MissingDetailsException extends NamingException {
    }

    private static class MissingUserException extends NamingException {
    }

    private static class TooManyUsersException extends NamingException {
    }

    private static LDAPDataSource singleton = null;

    private static final String
            PROP_BASE_DN_USER = "queries.basedn.user",
            PROP_QUERIES_USER = "queries.filter.user",
            PROP_BASE_DN_GROUPS = "queries.basedn.groups",
            PROP_QUERIES_GROUPS = "queries.filter.groups";
    private static final String[]
            USER_ATTRIBUTES = {"cn", "uid", "mail"};
    private static final String[] GROUP_ATTRIBUTES = {"cn"};

    private LDAPDataSource(final Hashtable<?, ?> environment) throws NamingException {
        super(environment);
    }

    static LDAPDataSource getInstance() throws NamingException {
        if (singleton == null) {
            singleton = new LDAPDataSource(ConfigProperties.getProperties(ConfigProperties.LDAP));
        }
        return singleton;
    }

    CertInfo getInfos(final String name) throws NamingException {
        final ConfigProperties props = ConfigProperties.getProperties(ConfigProperties.LDAP);
        final Formatter userFormatter = new Formatter();
        final Formatter groupFormatter = new Formatter();

        final String cn, uid, mail;
        final ArrayList<String> groups = new ArrayList<String>();

        final SearchControls user_controls = new SearchControls();
        user_controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        user_controls.setReturningAttributes(USER_ATTRIBUTES);

        final SearchControls groups_controls = new SearchControls();
        groups_controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        groups_controls.setReturningAttributes(GROUP_ATTRIBUTES);

        final String user_query = userFormatter.format(props.getProperty(PROP_QUERIES_USER), name).toString();
        final String groups_query = groupFormatter.format(props.getProperty(PROP_QUERIES_GROUPS), name).toString();

        final NamingEnumeration<SearchResult> user_results = this.search(props.getProperty(PROP_BASE_DN_USER), user_query, user_controls);
        if (!user_results.hasMore())
            throw new MissingUserException();
        final Attributes attributes = user_results.next().getAttributes();
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
            final SearchResult group = groups_results.next();
            final String groupCn = group.getAttributes().get("cn").get().toString();
            groups.add(groupCn);
        }

        return new CertInfo(cn, uid, mail, groups);
    }
}
