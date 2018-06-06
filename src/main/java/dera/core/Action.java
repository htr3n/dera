package dera.core;

import dera.util.CollectionUtil;
import dera.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class implements a simple actor that is responsible for performing
 * a particular task (e.g., invoking a Web rest, doing some
 * calculations, and so on). The actual behavior of this {@link Action}
 * must be set using the method {@link #setBehavior(Behavior)}.
 *
 */
public class Action extends AbstractEventActor {

    private static final Logger LOG = LoggerFactory.getLogger(Action.class);

    protected final Set<String> output;

    protected final ConcurrentLinkedQueue<Event> inputQueue;

    public Action(String id) {
        super(id);
        this.output = Collections.synchronizedSet(new HashSet<String>());
        this.inputQueue = new ConcurrentLinkedQueue<>();
    }

    public void addInput(String... events) {
        if (events != null && events.length > 0) {
            for (String event : events) {
                if (TextUtil.neitherNullNorEmpty(event)) {
                    this.input.add(event);
                    if (this.domain != null)
                        this.domain.bindInput(this, event);
                }
            }
        }
    }

    public void addInput(EventType... eventTypes) {
        if (CollectionUtil.neitherNullNorEmpty(eventTypes)) {
            for (EventType eventType : eventTypes) {
                if (eventType != null && TextUtil.neitherNullNorEmpty(eventType.getType())) {
                    this.input.add(eventType.getType());
                    if (this.domain != null)
                        this.domain.bindInput(this, eventType.getType());
                }
            }
        }
    }

    public void addOutput(String... events) {
        if (CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                if (TextUtil.neitherNullNorEmpty(event)) {
                    this.output.add(event);
                    if (this.domain != null)
                        this.domain.bindOutput(this, event);
                }
            }
        }
    }

    public void addOutput(EventType... eventTypes) {
        if (CollectionUtil.neitherNullNorEmpty(eventTypes)) {
            for (EventType eventType : eventTypes) {
                if (eventType != null && TextUtil.neitherNullNorEmpty(eventType.getType())) {
                    this.output.add(eventType.getType());
                    if (this.domain != null)
                        this.domain.bindOutput(this, eventType.getType());
                }
            }
        }
    }

    public void fire(final Event incomingEvent) {
        LOG.debug("{}[{}] => firing all outputs {}", new Object[]{getType(), getId(), this.output});
        final String correlationId = incomingEvent.getCorrelationId();
        if (CollectionUtil.neitherNullNorEmpty(this.output)) {
            publish(correlationId, incomingEvent.getAttributes(), this.output);
        }
    }

    @Override
    public Set<String> getOutput() {
        if (this.output != null)
            return this.output;
        else
            return Collections.<String>emptySet();
    }

    @Override
    public void notified(Event incomingEvent) {
        LOG.debug("Received {}", new Object[]{incomingEvent});
        if (this.behavior != null && this.domain != null) {
            this.domain.execute(incomingEvent, this);
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getType()).append("[").append(getId()).append("]");
        return buffer.toString();
    }

}
