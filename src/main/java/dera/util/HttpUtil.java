package dera.util;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtil {

    private static final String contentTypeRegEx = "(.*);(.*)";
    private static final Pattern contentTypePattern = Pattern.compile(contentTypeRegEx);
    private static final String charsetRegEx = "(?i)\\bcharset=\\s*\"?([^\\s;\"]*)";
    private static final Pattern charsetPattern = Pattern.compile(charsetRegEx);


    /**
     * Check if the input is a two-part content type, e.g., application/xml;charset=UTF-8
     *
     */
    public static boolean isTwoPartContentType(String contentType) {
        return contentType != null && contentType.matches(contentTypeRegEx);
    }

    /**
     * Get the first path of the two-part content type, e.g., application/xml;charset=UTF-8
     *
     */
    public static String extractContentType(String contentType) {
        if (isTwoPartContentType(contentType)) {
            Matcher matcher = contentTypePattern.matcher(contentType);
            if (matcher.find()) {
                String result = matcher.group(1);
                if (result != null)
                    return result.trim();
            }
        }
        return contentType;
    }

    public static String getCharsetFromContentType(String contentType) {
        if (contentType == null)
            return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find())
            return m.group(1).trim().toUpperCase();
        return null;
    }

    public static String getEncoding(final HttpServletRequest request) {
        String encoding = request.getHeader(HttpHeaders.CONTENT_ENCODING);
        return encoding == null || encoding.isEmpty() ? Consts.UTF_8.toString() : encoding;
    }

    public static int postJSON(String content, String uri) throws Exception {
        if (TextUtil.neitherNullNorEmpty(content)) {
            final HttpClient httpClient = HttpClientBuilder.create().build();
            final HttpPost httpPost = new HttpPost(uri);
            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            httpPost.setEntity(new StringEntity(content));
            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                final StatusLine statusLine = response.getStatusLine();
                return statusLine.getStatusCode();
            }
        }
        return -1;
    }

    public static int postXML(String content, String uri) throws Exception {
        if (TextUtil.neitherNullNorEmpty(content)) {
            final HttpClient httpClient = HttpClientBuilder.create().build();
            final HttpPost httpPost = new HttpPost(uri);
            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
            httpPost.setEntity(new StringEntity(content));
            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                final StatusLine statusLine = response.getStatusLine();
                return statusLine.getStatusCode();
            }
        }
        return -1;
    }

}
