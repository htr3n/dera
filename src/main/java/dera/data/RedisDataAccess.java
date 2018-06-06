package dera.data;

import dera.core.LifeCycle;
import dera.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class RedisDataAccess extends LifeCycle implements DataAccess<String> {

    private static final Logger LOG = LoggerFactory.getLogger(RedisDataAccess.class);
    private Jedis jedis;
    private String host;
    private int port;
    private static final int REDIS_DEFAULT_PORT = 6397;

    public RedisDataAccess() {
        this("localhost", REDIS_DEFAULT_PORT);
    }

    public RedisDataAccess(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void init() {
        try {
            jedis = new Jedis(host, port);
        } catch (Exception e) {
            LOG.warn("Unable to initialize the Jedis client" + e.getMessage());
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        try {
            jedis.quit();
        } catch (Exception e) {
            LOG.warn("Unable to close the Jedis client" + e.getMessage());
        }
    }

    @Override
    public void put(final String key, final String value){
        if (TextUtil.nullOrEmpty(key) || TextUtil.nullOrEmpty(value))
            return;
        if (jedis != null && jedis.isConnected())
            jedis.set(key, value);
    }

    @Override
    public String get(final String key){
        if (TextUtil.neitherNullNorEmpty(key) && jedis != null && jedis.isConnected())
            return jedis.get(key);
        return null;
    }

    @Override
    public void update(final String key, final String value){
        if (TextUtil.nullOrEmpty(key) || TextUtil.nullOrEmpty(value))
            return;
        if (jedis != null && jedis.isConnected())
            jedis.set(key, value);
    }

    @Override
    public String delete(final String key){
        if (TextUtil.neitherNullNorEmpty(key) && jedis != null && jedis.isConnected()) {
            String deleted = jedis.get(key);
            if (jedis.del(key) == 1)
                return deleted;
        }
        return null;
    }
}
