package dera.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class CollectionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionUtil.class);

    private CollectionUtil() {
    }

    public static Set newSet(Object... elements) {
        return new HashSet(java.util.Arrays.asList(elements));
    }

    public static Set newUnmodifiableSet(Object... elements) {
        return Collections.unmodifiableSet(new HashSet(java.util.Arrays.asList(elements)));
    }

    public static boolean nullOrEmpty(Collection c) {
        return (c == null || c.isEmpty());
    }

    public static boolean neitherNullNorEmpty(Collection c) {
        return (c != null && !c.isEmpty());
    }

    public static boolean nullOrEmpty(Map map) {
        return (map == null || map.isEmpty());
    }

    public static boolean neitherNullNorEmpty(Map map) {
        return (map != null && !map.isEmpty());
    }

    public static boolean nullOrEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }

    public static boolean neitherNullNorEmpty(Object[] array) {
        return (array != null && array.length > 0);
    }

    public static void print(Collection c) {
        if (c != null) {
            LOG.info("=========================================================================================");
            for (Object o : c) {
                LOG.info("{}", new Object[]{o});
            }
            LOG.info("=========================================================================================");
        }
    }

    public static void print(Map<Object, Object> m) {
        if (m != null) {
            LOG.info("=========================================================================================");
            for (Map.Entry entry : m.entrySet()) {
                LOG.info("{} => {}", new Object[]{entry.getKey(), entry.getValue()});
            }
            LOG.info("=========================================================================================");
        }
    }

}
