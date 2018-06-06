package dera.util;

import dera.core.Event;
import dera.rest.POST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DomainUtil {

    private static final Logger LOG = LoggerFactory.getLogger(DomainUtil.class);
    private static final String EVENT_PATH = "/domain/events";

    private DomainUtil() {
    }

    public static void forward(Event instance, String uri) {
        String content = null;
        try {
            content = JacksonUtil.getObjectToJsonMapper().convert(instance, "UTF-8");
        } catch (Exception e) {
            LOG.warn("Unable to convert the event ", e);
        }
        if (content != null) {
            POST.fireAndForget(uri, content);
        }
    }

    public static void forward(Event instance, String host, int port) {
        if (instance != null && TextUtil.neitherNullNorEmpty(host)) {
            String uri = new StringBuffer(host).append(port).append(EVENT_PATH).toString();
            forward(instance, uri);
        }
    }
}
