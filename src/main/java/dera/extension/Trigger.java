package dera.extension;

import dera.core.Action;
import dera.core.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class Trigger extends Action {

    private static final Logger LOG = LoggerFactory.getLogger(Trigger.class);

    public Trigger(String id) {
        super(id);
    }

    public void start(String correlationId) {
        LOG.info("Firing {} with the correlation ID {}", new Object[]{getOutput(), correlationId});
        publish(correlationId, Collections.EMPTY_MAP, output);
    }

    @Override
    public final void notified(final Event incoming) {
    }

    @Override
    public void addInput(String... events) {
        throw new UnsupportedOperationException();
    }

}
