package dera.util;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class RequestReply {

    private static final Logger log = LoggerFactory.getLogger(RequestReply.class);

    public static String send(HttpRequestBase httpRequest, String content) {
        try {
            final HttpClient httpClient = TextUtil.isSecureUri(httpRequest.getURI()) ? RESTClients.getSyncSecureHttpClient() : RESTClients.getSyncHttpClient();
            if (TextUtil.neitherNullNorEmpty(content)) {
                StringEntity entity = new StringEntity(content, ContentType.create(ContentType.APPLICATION_JSON.getMimeType(), Consts.UTF_8));
                ((HttpEntityEnclosingRequestBase)httpRequest).setEntity(entity);
            }
            HttpResponse response = httpClient.execute(httpRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                return RESTClients.getContent(response.getEntity());
            } else {
                log.error("Unsuccessful requestReply: {} - {}", new Object[]{statusCode, response.getStatusLine().getReasonPhrase()});
                return null;
            }
        } catch (IOException e) {
        }
        return null;
    }
}
