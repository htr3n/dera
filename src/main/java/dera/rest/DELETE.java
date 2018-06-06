package dera.rest;

import dera.util.FireAndForget;
import dera.util.RequestReply;
import dera.util.TextUtil;
import org.apache.http.client.methods.HttpDelete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public final class DELETE {

    private static final Logger LOG = LoggerFactory.getLogger(DELETE.class);

    private DELETE(){
    }

    public static void fireAndForget(String uri) {
        if (TextUtil.neitherNullNorEmpty(uri)) {
            try {
                final HttpDelete httpDelete = new HttpDelete(new URI(uri));
                FireAndForget.send(httpDelete, null);
            } catch (URISyntaxException e) {
                LOG.error("Invalid URI : " + uri, e);
            }
        }
    }

    public static String requestReply(String uri) {
        if (TextUtil.neitherNullNorEmpty(uri)) {
            try {
                final HttpDelete httpDelete = new HttpDelete(new URI(uri));
                return RequestReply.send(httpDelete, null);
            } catch (URISyntaxException e) {
                LOG.error("Invalid URI : " + uri, e);
            }
        }
        return null;
    }
}
