package dera.runtime;

import dera.core.Event;
import dera.core.EventType;
import dera.util.CollectionUtil;
import dera.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ChannelItem {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelItem.class);
    private ConcurrentHashMap<String, String> attributes;

    public ChannelItem() {
    }

    public void setAttributes(Map<String, String> attributes) {
        if (CollectionUtil.neitherNullNorEmpty(attributes)) {
            this.attributes = new ConcurrentHashMap<>();
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                if (!EventType.ATTR_TYPE.equals(entry.getKey())) {
                    this.attributes.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public void setAttribute(String name, String value) {
        if (TextUtil.neitherNullNorEmpty(name) && attributes.containsKey(name) && !EventType.ATTR_TYPE.equals(name)) {
            attributes.put(name, value);
        }
    }

    private void setType(String typeName) {
        attributes.put(EventType.ATTR_TYPE, typeName);
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getCorrelationId() {
        return this.attributes.get(EventType.ATTR_CORRELATION_ID);
    }

    public void setCorrelationId(String correlationId) {
        this.attributes.put(EventType.ATTR_CORRELATION_ID, correlationId);
    }

    public void cloneEvent(Event instance) {
        this.setAttributes(instance.getAttributes());
        this.setType(instance.getType());
    }

    public Event toEvent() {
        String type = this.getAttribute(EventType.ATTR_TYPE);
        EventType eventType = StandaloneExecutionDomain.getInstance().getEvent(type);
        if (eventType != null) {
            final Event instance = eventType.newInstance();
            for (String attr : attributes.keySet()) {
                try {
                    instance.setAttribute(attr, attributes.get(attr));
                } catch (Exception e) {
                }
            }
            return instance;
        } else {
            LOG.error("Failed to convert to an DERA event instance: unable to find the event type '{}'", new Object[]{type});
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(getClass().getSimpleName());
        buf.append("[type=").append(this.getAttribute(EventType.ATTR_TYPE));
        if (getCorrelationId() != null) {
            buf.append(", correlationId=").append(getCorrelationId());
        }
        buf.append("]");
        return buf.toString();
    }

}
