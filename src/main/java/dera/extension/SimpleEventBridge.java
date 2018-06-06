package dera.extension;

import dera.core.Bridge;
import dera.core.Event;
import dera.util.DomainUtil;
import dera.util.TextUtil;

public class SimpleEventBridge extends Bridge {

    public SimpleEventBridge(final String id, String host, int port, boolean secure) {
        super(id, host, port, secure);
    }

    @Override
    public void fire(Event incomingEvent) {
    }

    @Override
    public void notified(final Event incoming) {
        if (TextUtil.nullOrEmpty(host))
            return;
        StringBuffer uri = new StringBuffer();
        if (isSecure()) {
            uri.append("https://");
        } else {
            uri.append("http://");
        }
        uri.append(host)
                .append(":")
                .append(port)
                .append("/domain/events");
        DomainUtil.forward(incoming, uri.toString());
    }
}
