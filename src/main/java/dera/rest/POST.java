package dera.rest;

import dera.util.FireAndForget;
import dera.util.RequestReply;
import dera.util.TextUtil;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public final class POST {

    private static final Logger LOG = LoggerFactory.getLogger(POST.class);

    private POST(){
    }

    public static void fireAndForget(final String uri, final String content) {
        if (TextUtil.neitherNullNorEmpty(uri)) {
            try {
                final HttpPost httpPost = new HttpPost(new URI(uri));
                FireAndForget.send(httpPost, content);
            } catch (URISyntaxException e) {
                LOG.error("Invalid URI " + uri, e);
            }
        }
    }

    public static String requestReply(final String uri, final String content) {
        if (TextUtil.neitherNullNorEmpty(uri)) {
            try {
                final HttpPost httpPost = new HttpPost(new URI(uri));
                return RequestReply.send(httpPost, content);
            } catch (URISyntaxException e) {
                LOG.warn("Invalid URI " + uri, e);
            }
        }
        return null;
    }

}