package dera.frontend.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class WebSocketMonitor {

    private Session session;
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketMonitorServlet.class);
    private WebSocketMonitorServlet webSocketMonitorServlet;

    public WebSocketMonitor(WebSocketMonitorServlet webSocketMonitorServlet) {
        this.webSocketMonitorServlet = webSocketMonitorServlet;
    }

    public void sendMessage(String msg) throws IOException {
        if (session.isOpen()) {
            LOG.debug("... sending: '{}'", new Object[]{msg});
            session.getRemote().sendString(msg);
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        this.session = null;
        webSocketMonitorServlet.removeListener(this);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        webSocketMonitorServlet.addListener(this);
    }

    @OnWebSocketMessage
    public void onText(String message) {
        try {
            LOG.debug("Receive: {}", new Object[]{message});
            sendMessage(message);
        } catch (IOException e) {
        }
    }

    public boolean isOpen() {
        return session.isOpen();
    }
}
