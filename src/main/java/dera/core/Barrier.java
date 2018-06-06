package dera.core;

import dera.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link Barrier} is an AND-synchronization unit that must collect all
 * awaited events that are set using the {@link #addInput(String...)}
 * method before publishing all of its output events.
 *
 */
public class Barrier extends Action {

    private static final Logger LOG = LoggerFactory.getLogger(Barrier.class);
    protected final Map<String, Set<String>> correlationIdToEventIds;

    public Barrier(String id) {
        super(id);
        this.correlationIdToEventIds = new ConcurrentHashMap<>();
    }

    @Override
    public void notified(final Event incoming) {
        LOG.debug("{}[{}] received {}", new Object[]{getType(), getId(), incoming});
        boolean isPassed = updateBarrier(incoming);
        if (isPassed) {
            LOG.info("{}[{}] got all awaited events of correlation ID '{}' => fire {}", new Object[]{getType(), getId(), incoming.getCorrelationId(), output});
            publish(incoming.getCorrelationId(), incoming.getAttributes(), getOutput());
        }
    }

    private boolean updateBarrier(final Event event) {
        String correlationId = event.getCorrelationId();
        Set<String> events = this.correlationIdToEventIds.get(correlationId);
        if (CollectionUtil.nullOrEmpty(events)) { // first time
            events = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
            events.addAll(this.input);
            this.correlationIdToEventIds.put(correlationId, events);
        }
        events.remove(event.getType());
        if (events.isEmpty()) {
            this.correlationIdToEventIds.remove(correlationId);
        }
        return events.isEmpty();
    }
}
