package dera.core;

import dera.util.CollectionUtil;
import dera.util.TextUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EventType {

    public static final String EVENT_HIGH_PRIORITY_TYPE = "EVENT_HIGH_PRIORITY";
    public static final String EVENT_LOW_PRIORITY_TYPE = "EVENT_LOW_PRIORITY";

    public static final String ATTR_TYPE = "type";
    public static final String ATTR_CORRELATION_ID = "correlationId";

    private Set<String> attributes;
    private String type;
    private ExecutionDomain domain;

    /**
     * Create a new eventTypeName of event with a particular set of initial attributes (and possibly
     * clones some attributes from existing types)
     *
     * @param eventTypeName     the name of the new event eventTypeName
     * @param initialAttributes the list of initial initialAttributes of the event eventTypeName
     * @param inheritedTypes    the list of types where the properties are cloned
     */
    public EventType(String eventTypeName, String[] initialAttributes, EventType[] inheritedTypes) {
        this.type = eventTypeName;
        this.attributes = Collections.synchronizedSet(new HashSet<String>());

        this.attributes.add(ATTR_TYPE);
        this.attributes.add(ATTR_CORRELATION_ID);

        if (CollectionUtil.neitherNullNorEmpty(initialAttributes)) {
            for (String attr : initialAttributes) {
                this.attributes.add(attr);
            }
        }

        if (CollectionUtil.neitherNullNorEmpty(inheritedTypes)) {
            for (EventType eventType : inheritedTypes) {
                if (eventTypeName != null && CollectionUtil.neitherNullNorEmpty(eventType.getAttributes())) {
                    for (String attr : eventType.getAttributes()) {
                        this.attributes.add(attr);
                    }
                }
            }
        }
        this.dirty = true;
    }

    public Event newInstance() {
        return new Event(this.type, getAttributes());
    }

    public Set<String> getAttributes() {
        return this.attributes;
    }

    public String getType() {
        return this.type;
    }

    public String getQualifiedName() {
        if (this.domain != null && TextUtil.neitherNullNorEmpty(this.domain.getUri())) {
            return this.domain.getUri() + Namespace.NS_SEPARATOR + this.type;
        } else {
            return this.type;
        }
    }

    public ExecutionDomain getDomain() {
        return this.domain;
    }

    public void setDomain(ExecutionDomain domain) {
        this.domain = domain;
    }

    public boolean match(final EventType other) {
        if (other == null
                || TextUtil.nullOrEmpty(other.getQualifiedName())
                || !other.getQualifiedName().equals(getQualifiedName())) {
            return false;
        }
        return this.getAttributes().equals(other.getAttributes());
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(getClass().getSimpleName());
        buf.append("[type=").append(this.type);
        if (CollectionUtil.neitherNullNorEmpty(getAttributes())) {
            buf.append(", attributes={");
            for (String name : getAttributes()) {
                buf.append(name).append(" ");
            }
            buf.append("}");
        }
        buf.append("]");
        return buf.toString();
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    protected boolean dirty;
}
