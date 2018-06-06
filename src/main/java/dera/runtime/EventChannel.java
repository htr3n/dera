package dera.runtime;

import dera.core.EventActor;
import dera.core.Event;
import dera.core.EventType;
import dera.core.LifeCycle;
import dera.util.TextUtil;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class EventChannel extends LifeCycle {

    public static final EventFactory<ChannelItem> EVENT_FACTORY = new EventFactory<ChannelItem>() {
        public ChannelItem newInstance() {
            return new ChannelItem();
        }
    };
    private static final Logger LOG = LoggerFactory.getLogger(EventChannel.class);
    private RingBuffer<ChannelItem> ringBuffer;
    private Disruptor<ChannelItem> disruptor;
    private EventDistributor distributor;
    private StandaloneExecutionDomain domain;

    public EventChannel(StandaloneExecutionDomain domain) {
        this.distributor = new EventDistributor();
        this.domain = domain;
    }

    public void send(final Event instance) {
        LOG.debug("Publishing '{}'", new Object[]{instance});
        if (instance != null) {
            String correlationId = instance.getCorrelationId();
            try {
                long sequence = ringBuffer.next();
                ChannelItem item = ringBuffer.get(sequence);
                if (item != null) {
                    item.cloneEvent(instance);
                    item.setCorrelationId(TextUtil.nullOrEmpty(correlationId) ? TextUtil.randomFixedLengthNumber() : correlationId);
                    ringBuffer.publish(sequence);
                } else {
                    LOG.error("Cannot get the next place in the ring buffer. Unable to publish '" + instance + "'");
                }
            } catch (Exception e) {
                LOG.debug("Unable to send '" + instance + "'", e);
            }
        }
    }

    @Override
    public void init() {
    }

    public void start() {
        disruptor = new Disruptor<>(
                EVENT_FACTORY,
                domain.getDaemonExecutor(),
                new MultiThreadedLowContentionClaimStrategy(domain.getConfiguration().getMaxChannelSize()),
                new SleepingWaitStrategy());
        disruptor.handleEventsWith(distributor);
        ringBuffer = disruptor.start();
        LOG.info("Started the event channel of the domain '{}' with size [{}]", new Object[]{domain.getId(), domain.getConfiguration().getMaxChannelSize()});
        switchState();
    }

    public void stop() {
        if (disruptor != null) {
            disruptor.halt();
        }
        LOG.info("The event channel of the domain '{}' is stopped", new Object[]{domain.getId()});
        switchState();
    }

    class EventDistributor implements EventHandler<ChannelItem> {
        @Override
        public void onEvent(final ChannelItem channelItem, final long sequence, final boolean endOfBatch) {
            if (channelItem == null)
                return;
            Event event = channelItem.toEvent();
            if (event != null) {
                LOG.debug("Start distributing " + event);

                // deliver to the actors registered to the high priority event type
                deliver(event, domain.getEventActorManager().getConsumers(EventType.EVENT_HIGH_PRIORITY_TYPE));

                // deliver to the subscribed actors
                //deliverIfMatched(instance, domain.getEventActorManager().getConsumers(instance.getType()));
                deliver(event, domain.getEventActorManager().getConsumers(event.getType()));

                // deliver to the actors registered to the low priority event type
                deliver(event, domain.getEventActorManager().getConsumers(EventType.EVENT_LOW_PRIORITY_TYPE));
            } else {
                LOG.error("Unable to convert the internal {} to an DERA event instance for publishing", new Object[]{channelItem});
            }
        }

        private void deliverIfMatched(final Event event, Set<EventActor> actors) {
            if (actors != null && !actors.isEmpty()) {
                LOG.debug("... distributing {} to '{}'", new Object[]{event, actors});
                for (final EventActor actor : actors) {
                    if (actor != null) {
                        actor.notified(event);
                    }
                }
            }
        }

        private void deliver(final Event instance, Set<EventActor> actors) {
            if (actors != null && !actors.isEmpty()) {
                LOG.debug("... distributing {} to {}", new Object[]{instance, actors});
                for (EventActor actor : actors) {
                    if (actor != null) {
                        actor.notified(instance);
                    }
                }
            }
        }
    }
}
