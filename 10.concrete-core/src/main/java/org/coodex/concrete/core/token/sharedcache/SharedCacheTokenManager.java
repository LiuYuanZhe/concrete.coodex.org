package org.coodex.concrete.core.token.sharedcache;

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenManager;
import org.coodex.sharedcache.SharedCacheClient;
import org.coodex.sharedcache.SharedCacheClientManager;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public class SharedCacheTokenManager implements TokenManager {

//    long maxIdleTime = ConcreteHelper.getProfile().getLong("sharedCacheTokenManager.maxIdleTime", DEFAULT_MAX_IDLE) * 60 * 1000;

    @Override
    public Token getToken(String id) {
        return getToken(id, false);
    }

    @Override
    public Token getToken(String id, boolean force) {
        String tokenCacheType = ConcreteHelper.getProfile().getString("tokenCacheType");
        SharedCacheClient client = SharedCacheClientManager.getClient(tokenCacheType);
        long maxIdleTime = ConcreteHelper.getProfile().getLong("sharedCacheTokenManager.maxIdleTime", DEFAULT_MAX_IDLE) * 60 * 1000;

        return new SharedCacheToken(client, id, maxIdleTime);
    }
}
