package dera.frontend.websocket;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

public class MonitorWebSocketCreator implements WebSocketCreator {

    private WebSocketMonitorServlet webSocketMonitorServlet;

    public MonitorWebSocketCreator(WebSocketMonitorServlet webSocketMonitorServlet) {
        this.webSocketMonitorServlet = webSocketMonitorServlet;
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        for (String protocol : req.getSubProtocols()) {
            switch (protocol) {
                case "htr3n.dera.monitor":
                    return new WebSocketMonitor(webSocketMonitorServlet);
                default:
                    return null;
            }
        }
        return null;
    }
}
