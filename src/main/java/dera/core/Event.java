package dera.core;

import dera.error.InvalidEventAttributeException;
import dera.error.NoSuchEventAttributeException;
import dera.error.UnmodifiableEventAttributeException;
import dera.util.CollectionUtil;
import dera.util.TextUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Event {

    private Map<String, String> attributes;
    private String internalId;

    protected Event(String eventType, Set<String> attributeNames) {
        this.internalId = TextUtil.randomId("event");
        this.attributes = new HashMap<>();
        if (CollectionUtil.neitherNullNorEmpty(attributeNames)) {
            for (String attributeName : attributeNames){
                this.attributes.put(attributeName, null);
            }
        }
        this.attributes.put(EventType.ATTR_TYPE, eventType);
        this.attributes.put(EventType.ATTR_CORRELATION_ID, TextUtil.randomFixedLengthString());
    }

    public Set<String> getAttributeNames() {
        return Collections.unmodifiableSet(attributes.keySet());
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public String setAttribute(String name, String value) throws InvalidEventAttributeException, NoSuchEventAttributeException, UnmodifiableEventAttributeException {
        if (TextUtil.nullOrEmpty(name)){
            throw new InvalidEventAttributeException(name);
        }
        if (!attributes.containsKey(name)){
            throw new NoSuchEventAttributeException(name);
        }
        if (EventType.ATTR_TYPE.equals(name)){
            throw new UnmodifiableEventAttributeException();
        }
        return attributes.put(name, value);
    }

    public String getAttribute(String name) throws InvalidEventAttributeException, NoSuchEventAttributeException {
        if (TextUtil.nullOrEmpty(name)){
            throw new InvalidEventAttributeException(name);
        }
        if (!attributes.containsKey(name)){
            throw new NoSuchEventAttributeException(name);
        }
        return attributes.get(name);
    }

    public String getCorrelationId() {
        return this.attributes.get(EventType.ATTR_CORRELATION_ID);
    }

    public void setCorrelationId(String correlationId) {
        this.attributes.put(EventType.ATTR_CORRELATION_ID, correlationId);
    }

    public String getType() {
        return this.attributes.get(EventType.ATTR_TYPE);
    }

    public String getId() {
        return internalId;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(getClass().getSimpleName());
        buf.append("[type=").append(getType());
        if (getCorrelationId() != null){
            buf.append(", correlationId=").append(getCorrelationId());
        }
        buf.append("]");
        return buf.toString();
    }


}
