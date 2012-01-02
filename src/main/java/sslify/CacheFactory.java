package sslify;

import net.sf.ehcache.Cache;

import java.io.FileNotFoundException;

public interface CacheFactory {
    Cache getCache(Domain domain) throws FileNotFoundException;

    public enum Domain {LDAP, REPOSITORY}
}
