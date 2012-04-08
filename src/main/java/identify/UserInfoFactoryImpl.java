package identify;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

@Slf4j
@Singleton
public class UserInfoFactoryImpl implements UserInfoFactory {
    private static final String PROP_BASE_DN_USER = "queries.basedn.users";
    private static final String PROP_BASE_DN_GROUPS = "queries.basedn.groups";
    private static final String PROP_FILTER_USERS = "queries.filter.users";
    private static final String PROP_FILTER_GROUPS = "queries.filter.groups";
    private static final String PROP_KEY_USER = "queries.key.user";
    private static final String PROP_KEY_GROUP = "queries.key.group";

    private static SearchControls groupSearchControls, usersSearchControls;
    private final ConfigProperties props;

    @Inject
    UserInfoFactoryImpl(final ConfigPropertiesFactory configPropertiesFactory)
            throws FileNotFoundException, ConfigProperties.ConfigLoadingException {
        this.props = configPropertiesFactory.get(ConfigProperties.Domain.LDAP);
        groupSearchControls = buildGroupControls();
        usersSearchControls = buildUsersControls();
    }

    private SearchControls buildUsersControls() {
        final SearchControls users_controls = new SearchControls();
        users_controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        users_controls.setReturningAttributes(new String[] {"cn", "uid", "mail", "mobile", "departmentNumber", "title", "l"});
        return users_controls;
    }

    private SearchControls buildGroupControls() {
        final SearchControls groups_controls = new SearchControls();
        groups_controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        groups_controls.setReturningAttributes(new String[] {(String) props.get(PROP_KEY_GROUP)});
        return groups_controls;
    }

    private String buildGroupQuery(String user) {
        final Formatter formatter = new Formatter();
        return formatter.format(props.getProperty(PROP_FILTER_GROUPS), user).toString();
    }

    private UserInfo getUser(SearchResult sr) throws NamingException {
        final String cn, uid, mail;
        String mobile = null, department = null, title = null, l = null;
        final ArrayList<String> groups = new ArrayList<String>();
        final Attributes attributes = sr.getAttributes();
        final InitialDirContext dirContext = new InitialDirContext(this.props);

        try {
            cn = attributes.get("cn").get().toString();
            uid = attributes.get("uid").get().toString();
            mail = attributes.get("mail").get().toString();
        } catch (NullPointerException e) {
            throw new MissingDetailsException();
        }
        try {
            mobile = attributes.get("mobile").get().toString();
        } catch (NullPointerException ignored) {}
        try {
            department = attributes.get("department").get().toString();
        } catch (NullPointerException ignored) {}
        try {
            title = attributes.get("title").get().toString();
        } catch (NullPointerException ignored) {}
        try {
            l = attributes.get("l").get().toString();
        } catch (NullPointerException ignored) {}

        final NamingEnumeration<SearchResult> groups_results =
                dirContext.search(props.getProperty(PROP_BASE_DN_GROUPS),
                        buildGroupQuery(attributes.get(props.getProperty(PROP_KEY_USER)).get().toString()), groupSearchControls);
        while (groups_results.hasMore()) {
            groups.add(groups_results.next().getAttributes().get((String) props.get(PROP_KEY_GROUP)).get().toString());
        }

        dirContext.close();

        return new UserInfo(cn, uid, mail, mobile, department, title, l, groups.toArray(new String[groups.size()]));
    }

    @Override
    public List<UserInfo> getUsers() throws NamingException {
        List<UserInfo> users = new ArrayList<UserInfo>();
        final InitialDirContext dirContext = new InitialDirContext(this.props);
        final NamingEnumeration<SearchResult> users_results =
                dirContext.search(props.getProperty(PROP_BASE_DN_USER),
                        props.getProperty(PROP_FILTER_USERS), usersSearchControls);

        while (users_results.hasMore()) {
            users.add(getUser(users_results.next()));
        }

        return users;
    }
}
