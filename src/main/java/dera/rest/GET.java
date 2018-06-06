package dera.rest;

import dera.util.FireAndForget;
import dera.util.RequestReply;
import dera.util.TextUtil;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public final class GET {

    private static final Logger LOG = LoggerFactory.getLogger(GET.class);

    private GET(){
    }

    public static void fireAndForget(String uri) {
        if (TextUtil.neitherNullNorEmpty(uri)) {
            try {
                final HttpGet httpGet = new HttpGet(new URI(uri));
                FireAndForget.send(httpGet, null);
            } catch (URISyntaxException e) {
                LOG.error("Invalid URI : " + uri, e);
            }
        }
    }

    public static String requestReply(String uri) {
        if (TextUtil.neitherNullNorEmpty(uri)) {
            try {
                final HttpGet httpGet = new HttpGet(new URI(uri));
                return RequestReply.send(httpGet, null);
            } catch (URISyntaxException e) {
                LOG.error("Invalid URI : " + uri, e);
            }
        }
        return null;
    }
}
