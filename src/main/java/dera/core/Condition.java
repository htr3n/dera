package dera.core;

import dera.util.CollectionUtil;
import dera.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Condition extends Action {

    private static final Logger LOG = LoggerFactory.getLogger(Condition.class);
    protected Set<String> trueEvent;
    protected Set<String> falseEvent;
    protected Predicate predicate;

    public Condition(String id) {
        super(id);
        this.falseEvent = Collections.synchronizedSet(new HashSet<String>());
        this.trueEvent = Collections.synchronizedSet(new HashSet<String>());
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public void setPredicate(final Predicate predicate) {
        this.predicate = predicate;
    }

    public void addTrueEvent(String... events) {
        if (CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                if (TextUtil.neitherNullNorEmpty(event)) {
                    trueEvent.add(event);
                    try {
                        domain.bindOutput(this, event);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public void addTrueEvent(EventType... eventTypes) {
        if (CollectionUtil.neitherNullNorEmpty(eventTypes)) {
            for (EventType eventType : eventTypes) {
                if (eventType != null && TextUtil.neitherNullNorEmpty(eventType.getType())) {
                    trueEvent.add(eventType.getType());
                    try {
                        domain.bindOutput(this, eventType.getType());
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public void removeTrueEvent(String... events) {
        if (CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                if (TextUtil.neitherNullNorEmpty(event)) {
                    trueEvent.remove(event);
                    try {
                        domain.unbindOutput(this, event);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public void addFalseEvent(String... events) {
        if (CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                if (TextUtil.neitherNullNorEmpty(event)) {
                    falseEvent.add(event);
                    try {
                        domain.bindOutput(this, event);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public void addFalseEvent(EventType... eventTypes) {
        if (CollectionUtil.neitherNullNorEmpty(eventTypes)) {
            for (EventType eventType : eventTypes) {
                if (eventType != null && TextUtil.neitherNullNorEmpty(eventType.getType())) {
                    falseEvent.add(eventType.getType());
                    try {
                        domain.bindOutput(this, eventType.getType());
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public void removeFalseEvent(final String... events) {
        if (CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                if (TextUtil.neitherNullNorEmpty(event)) {
                    falseEvent.remove(event);
                    try {
                        domain.unbindOutput(this, event);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @Override
    public void notified(final Event incomingEvent) {
        LOG.debug("{}[{}] received {}", new Object[]{getType(), getId(), incomingEvent});
        String correlationId = incomingEvent.getCorrelationId();
        if (predicate != null) {
            if (predicate.eval()) {
                LOG.debug("{}[{}] => TRUE: publishing {} with the correlation Id '{}'", new Object[]{getType(), getId(), trueEvent, correlationId});
                publish(correlationId, incomingEvent.getAttributes(), trueEvent);
            } else {
                LOG.debug("{}[{}] => FALSE: publishing {} with the correlation Id '{}'", new Object[]{getType(), getId(), falseEvent, correlationId});
                publish(correlationId, incomingEvent.getAttributes(), falseEvent);
            }
        } else {
            LOG.error("{}[{}] does not have a valid Predicate! Do nothing!", new Object[]{getType(), getId()});
        }
    }

    public Set<String> getTrueEvent() {
        return (trueEvent != null) ? trueEvent : Collections.EMPTY_SET;
    }

    public Set<String> getFalseEvent() {
        return (falseEvent != null) ? falseEvent : Collections.EMPTY_SET;
    }

    @Override
    public final void addOutput(String... events) {
        throw new UnsupportedOperationException();
    }
}
