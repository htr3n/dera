package dera.runtime;

import dera.core.EventActor;
import dera.core.Behavior;
import dera.core.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExecutableEntry implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutableEntry.class);
    private final Event instance;
    private final EventActor actor;

    public ExecutableEntry(final Event instance, final EventActor actor) {
        this.instance = instance;
        this.actor = actor;
    }

    public Event getEventInstance() {
        return instance;
    }

    public EventActor getActor() {
        return actor;
    }

    @Override
    public void run() {
        if (actor != null) {
            LOG.debug("Enacting the behavior of {}[{}] regarding the event type {} with the correlation ID '{}'",
                    new Object[]{actor.getType(), actor.getId(), instance.getType(), instance.getCorrelationId()});
            Behavior behavior = actor.getBehavior();

            //TODO: should we use a dedicated execution queue
            if (behavior != null){
                behavior.run(instance);
            }
        }
    }

    @Override
    public String toString() {
        return new StringBuffer(getClass().getSimpleName())
                .append(" => {")
                .append(actor.getType())
                .append("[")
                .append(actor.getId())
                .append("], event-instance[")
                .append(instance.getType())
                .append("]}")
                .toString();
    }
}
