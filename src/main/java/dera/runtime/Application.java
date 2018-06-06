package dera.runtime;

import dera.core.EventType;
import dera.util.CollectionUtil;
import dera.util.TextUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Application {

    private String id;
    private Set<String> start;
    private Set<String> end;

    @JsonIgnore
    private final AtomicLong instanceNumber = new AtomicLong(1);

    public Application() {
        this(TextUtil.randomId("app"));
    }

    public Application(String appId) {
        this.id = appId;
        this.start = Collections.synchronizedSet(new HashSet<String>());
        this.end = Collections.synchronizedSet(new HashSet<String>());
    }

    public ApplicationInstance createInstance(final String correlationId) {
        final ApplicationInstance instance = new ApplicationInstance(this, correlationId);
        instance.setId(getId() + "-instance-" + instanceNumber.getAndIncrement());
        return instance;
    }

    public void addStart(String... events) {
        if (CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                if (event != null) {
                    start.add(event);
                }
            }
        }
    }

    public void addStart(EventType... eventTypes) {
        if (CollectionUtil.neitherNullNorEmpty(eventTypes)) {
            for (EventType eventType : eventTypes) {
                if (eventType != null && TextUtil.neitherNullNorEmpty(eventType.getType())) {
                    start.add(eventType.getType());
                }
            }
        }
    }

    public void removeStart(String... events) {
        if (CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                if (event != null) {
                    start.remove(event);
                }
            }
        }
    }

    public void addEnd(String... events) {
        if (CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                if (event != null) {
                    end.add(event);
                }
            }
        }
    }

    public void addEnd(EventType... eventTypes) {
        if (CollectionUtil.neitherNullNorEmpty(eventTypes)) {
            for (EventType eventType : eventTypes) {
                if (eventType != null && TextUtil.neitherNullNorEmpty(eventType.getType()))
                    end.add(eventType.getType());
            }
        }
    }

    public void removeEnd(String... events) {
        if (CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                if (event != null) {
                    end.remove(event);
                }
            }
        }
    }

    public Set<String> getStart() {
        return start;
    }

    public Set<String> getEnd() {
        return end;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return new StringBuffer(getClass().getSimpleName())
                .append(" ")
                .append(getId())
                .append(" :: start ")
                .append(getStart())
                .append(" :: end ")
                .append(getEnd())
                .toString();
    }
}
