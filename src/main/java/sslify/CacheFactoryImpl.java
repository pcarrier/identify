package sslify;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import java.io.FileNotFoundException;
import java.util.EnumMap;
import java.util.Map;

public class CacheFactoryImpl implements CacheFactory {
    static final String configFileName = "ehcache.xml";

    private static Map<Domain, String> mappings =
            new EnumMap<Domain, String>(Domain.class);

    static {
        mappings.put(Domain.LDAP, "ldap");
        mappings.put(Domain.REPOSITORY, "repository");
    }

    private static Map<Domain, Cache> loaded =
            new EnumMap<Domain, Cache>(Domain.class);

    /* Optimisticly non-synchronized */
    @Override
    public Cache getCache(Domain domain) throws FileNotFoundException {
        if (loaded.containsKey(domain)) {
            return loaded.get(domain);
        }
        CacheManager cacheManager = CacheManager.create(ConfigFileLocator.getInputStream(configFileName));
        Cache cache = cacheManager.getCache(mappings.get(domain));
        loaded.put(domain, cache);
        return cache;
    }
}
