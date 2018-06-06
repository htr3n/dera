package dera.util;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.nio.client.HttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FireAndForget {

    private static final Logger log = LoggerFactory.getLogger(FireAndForget.class);


    public static void send(final HttpRequestBase httpRequest, String content) {
        final HttpAsyncClient httpClient = TextUtil.isSecureUri(httpRequest.getURI()) ? RESTClients.getAsyncSecureHttpClient() : RESTClients.getAsyncHttpClient();
        if (TextUtil.neitherNullNorEmpty(content)) {
            StringEntity entity = new StringEntity(content, ContentType.create(ContentType.APPLICATION_JSON.getMimeType(), Consts.UTF_8));
            ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(entity);
        }
        Future<HttpResponse> future = httpClient.execute(httpRequest, null);
        try {
            HttpResponse response = future.get();
            log.debug("fire-and-forget response: " + response.getStatusLine());
        } catch (InterruptedException e) {
            log.debug("Error: ", e);
        } catch (ExecutionException e) {
            log.debug("Error: ", e);
        } finally {
            httpRequest.releaseConnection();
        }
    }

}
