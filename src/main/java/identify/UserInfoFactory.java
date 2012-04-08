package identify;

import javax.naming.NamingException;
import java.util.List;

public interface UserInfoFactory {
    public List<UserInfo> getUsers() throws NamingException;
    public static class MissingDetailsException extends NamingException {}
}
