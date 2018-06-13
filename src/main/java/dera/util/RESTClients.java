package dera.util;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public final class RESTClients {

    private static final Logger log = LoggerFactory.getLogger(RESTClients.class);

    private static final CloseableHttpClient syncHttpClient;
    private static final CloseableHttpAsyncClient asyncHttpClient;
    private static final CloseableHttpClient syncSecureHttpClient;
    private static final CloseableHttpAsyncClient asyncSecureHttpClient;

    public static HttpClient getSyncHttpClient() {
        return syncHttpClient;
    }

    public static HttpAsyncClient getAsyncHttpClient() {
        return asyncHttpClient;
    }

    public static CloseableHttpClient getSyncSecureHttpClient() {
        return syncSecureHttpClient;
    }

    public static CloseableHttpAsyncClient getAsyncSecureHttpClient() {
        return asyncSecureHttpClient;
    }

    public static void stopAllClients() {
        stopSyncHttpClient();
        stopSyncHttpsClient();
        stopAsyncHttpClient();
        stopAsyncHttpsClient();
    }

    public static void stopSyncHttpClient() {
        try {
            syncHttpClient.close();
        } catch (IOException e) {
            log.error("Unable to stop the synchronous HTTP client: ", e);
        }
    }

    public static void stopSyncHttpsClient() {
        try {
            syncSecureHttpClient.close();
        } catch (IOException e) {
            log.error("Unable to stop the secure synchronous HTTP client: ", e);
        }
    }

    public static void stopAsyncHttpClient() {
        try {
            asyncHttpClient.close();
        } catch (IOException e) {
            log.error("Unable to stop the asynchronous HTTP client: ", e);
        }
    }

    public static void stopAsyncHttpsClient() {
        try {
            asyncSecureHttpClient.close();
        } catch (IOException e) {
            log.error("Unable to stop the secure asynchronous HTTP client: ", e);
        }
    }

    static {
        syncHttpClient = createSyncHttpClient();
        syncSecureHttpClient = createSecureSyncHttpClient();
        asyncHttpClient = createAsyncHttpClient();
        asyncSecureHttpClient = createSecureAsyncHttpClient();
    }

    private static CloseableHttpClient createSyncHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        return builder.build();
    }

    private static CloseableHttpClient createSecureSyncHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setSSLSocketFactory(new SSLConnectionSocketFactory(
                SSLContexts.createDefault(),
                NoopHostnameVerifier.INSTANCE));
        return builder.build();
    }

    private static CloseableHttpAsyncClient createAsyncHttpClient() {
        final CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.createDefault();
        httpAsyncClient.start();
        return httpAsyncClient;
    }

    private static CloseableHttpAsyncClient createSecureAsyncHttpClient() {
        HttpAsyncClientBuilder builder = HttpAsyncClientBuilder.create();
        builder.setSSLStrategy(new SSLIOSessionStrategy(
                SSLContexts.createDefault(),
                NoopHostnameVerifier.INSTANCE));
        final CloseableHttpAsyncClient httpAsyncClient = builder.build();
        httpAsyncClient.start();
        return httpAsyncClient;
    }

    public static String getContent(HttpEntity entity) {
        if (entity != null) {
            try {
                InputStream inputStream = entity.getContent();
                String charsetName = Consts.UTF_8.toString();
                Header encoding = entity.getContentEncoding();
                if (encoding != null && encoding.getValue() != null) {
                    charsetName = encoding.getValue();
                }
                return TextUtil.convert(inputStream, charsetName);
            } catch (IOException e) {
            }
        }
        return null;
    }

}
