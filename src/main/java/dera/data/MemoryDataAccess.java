package dera.data;

import dera.core.LifeCycle;
import dera.util.TextUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryDataAccess extends LifeCycle implements DataAccess<String> {

    private Map<String, String> objectMap;

    @Override
    public void init() {
    }

    @Override
    public void start() {
        this.objectMap = new ConcurrentHashMap<String, String>();
    }

    @Override
    public void stop() {
        if (this.objectMap != null) {
            this.objectMap.clear();
        }
        this.objectMap = null;
    }

    @Override
    public void put(final String key, final String value) {
        if (TextUtil.neitherNullNorEmpty(key) && TextUtil.neitherNullNorEmpty(value)) {
            this.objectMap.put(key, value);
        }
    }

    @Override
    public String get(final String key) {
        if (TextUtil.neitherNullNorEmpty(key)) {
            return this.objectMap.get(key);
        }
        return null;
    }

    @Override
    public void update(final String key, final String value) {
        if (TextUtil.neitherNullNorEmpty(key) && TextUtil.neitherNullNorEmpty(value)) {
            this.objectMap.put(key, value);
        }
    }

    @Override
    public String delete(final String key) {
        if (TextUtil.neitherNullNorEmpty(key)) {
            return this.objectMap.remove(key);
        }
        return null;
    }
}
