package dera.runtime;

import dera.core.EventActor;
import dera.DERA;
import dera.core.EventType;
import dera.core.LifeCycle;
import dera.error.ActorExistedException;
import dera.error.ActorNotExistedException;
import dera.error.EventTypeExistedException;
import dera.error.EventTypeNotExistedException;
import dera.util.CollectionUtil;
import dera.util.TextUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventActorManager extends LifeCycle {

    private static final Logger LOG = LoggerFactory.getLogger(EventActorManager.class);
    private Map<String, EventActor> idToActor;
    private Map<String, EventActorState> idToActorState;
    private Map<String, SubscriptionEntry> eventToPubSub;
    private StandaloneExecutionDomain domain;

    public EventActorManager(StandaloneExecutionDomain domain) {
        this.domain = domain;
    }

    @Override
    public void init() {
        idToActor = new ConcurrentHashMap<>();
        idToActorState = new ConcurrentHashMap<>();
        eventToPubSub = new ConcurrentHashMap<>();
        try {
            addEvent(EventType.EVENT_HIGH_PRIORITY_TYPE);
            addEvent(EventType.EVENT_LOW_PRIORITY_TYPE);
        } catch (Exception e) {
        }
    }

    @Override
    public void start() {
        switchState();
    }

    @Override
    public void stop() {
        switchState();
    }

    public void addEvent(String event) throws EventTypeNotExistedException, EventTypeExistedException {
        if (TextUtil.neitherNullNorEmpty(event)) {
            addEvent(event, null, null);
        }
    }

    public void addEvent(String event, String[] attributes, EventType[] templates) throws EventTypeNotExistedException, EventTypeExistedException {
        if (TextUtil.nullOrEmpty(event)) {
            throw new EventTypeNotExistedException(event);
        }
        if (eventToPubSub.containsKey(event)) {
            throw new EventTypeExistedException(event);
        }
        EventType newEventType = DERA.newEventType(event, attributes, templates, domain);
        newEventType.setDomain(domain);
        newEventType.setDirty(true);
        addSubscriptionEntry(newEventType);
    }

    private void addSubscriptionEntry(EventType eventType) {
        if (TextUtil.neitherNullNorEmpty(eventType.getType())) {
            SubscriptionEntry entry = new SubscriptionEntry(eventType);
            eventToPubSub.put(eventType.getType(), entry);
        }
    }

    public void addEvent(EventType eventType) throws EventTypeExistedException {
        if (eventType != null && TextUtil.neitherNullNorEmpty(eventType.getType())) {
            if (eventToPubSub.containsKey(eventType.getType())) {
                throw new EventTypeExistedException(eventType.getType());
            }
            eventType.setDomain(domain);
            eventType.setDirty(true);
            addSubscriptionEntry(eventType);
        }
    }

    public final void removeEvent(String event) throws EventTypeNotExistedException {
        if (TextUtil.neitherNullNorEmpty(event)) {
            if (!eventToPubSub.containsKey(event)) {
                throw new EventTypeNotExistedException(event);
            }
            eventToPubSub.remove(event);
            //SubscriptionEntry entry = eventToPubSub.remove(event);
            //TODO do something regarding the removed event types !!!
            //TODO update the event-to-* lists and actors' inputs & outputs!!!
        }
    }

    public EventType getEvent(String event) {
        if (event != null) {
            SubscriptionEntry entry = eventToPubSub.get(event);
            if (entry != null) {
                return entry.getEventType();
            }
        }
        return null;
    }

    public final void register(final EventActor actor) throws ActorExistedException {
        if (actor != null) {
            String actorId = actor.getId();
            if (!idToActor.containsKey(actorId)) {
                actor.setDomain(domain);
                idToActor.put(actorId, actor);
                idToActorState.put(actorId, EventActorState.ENABLING);
                if (CollectionUtil.neitherNullNorEmpty(actor.getInput())){
                    pairInput(actor, actor.getInput());
                }
                if (CollectionUtil.neitherNullNorEmpty(actor.getOutput())){
                    pairOutput(actor, actor.getOutput());
                }
                LOG.debug("Registered {}[{}], input = {}, output = {}, state = {}", new Object[]{actor.getType(), actorId, actor.getInput(), actor.getOutput(), idToActorState.get(actorId)});
            } else {
                throw new ActorExistedException(actorId);
            }
        }
    }

    public final void deregister(String actorId) throws ActorNotExistedException {
        if (TextUtil.neitherNullNorEmpty(actorId)) {
            if (!idToActor.containsKey(actorId)) {
                throw new ActorNotExistedException(actorId);
            } else {
                EventActor unregisteredActor = idToActor.remove(actorId);
                unpairInput(unregisteredActor, unregisteredActor.getInput());
                unpairOutput(unregisteredActor, unregisteredActor.getOutput());
                idToActorState.remove(actorId);
                LOG.debug("Unregistered {}[{}]", new Object[]{unregisteredActor.getType(), unregisteredActor.getId()});
            }
        }
    }

    public final void pairInput(final EventActor actor, final Set<String> events) {
        if (actor != null && CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                if (event != null) {
                    pairInput(actor, event);
                }
            }
        }
    }

    public final void unpairInput(EventActor actor, final Set<String> events) {
        if (actor != null && CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                if (TextUtil.neitherNullNorEmpty(event)) {
                    unpairInput(actor, event);
                }
            }
        }
    }

    public void pairOutput(final EventActor actor, Set<String> events) {
        if (actor != null && CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                if (TextUtil.neitherNullNorEmpty(event))
                    pairOutput(actor, event);
            }
        }
    }

    public void pairOutput(final EventActor actor, String event) {
        if (actor != null && TextUtil.neitherNullNorEmpty(event)) {
            SubscriptionEntry entry = eventToPubSub.get(event);
            if (entry != null) {
                LOG.debug("{}[{}] is paired as a producer of the event type '{}'", new Object[]{actor.getType(), actor.getId(), event});
                entry.addProducer(actor);
            }
        }
    }

    public void unpairOutput(final EventActor producer, Set<String> events) {
        if (producer != null && CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                if (TextUtil.neitherNullNorEmpty(event))
                    unpairOutput(producer, event);
            }
        }
    }

    public void unpairOutput(final EventActor producer, String event) {
        if (producer != null && TextUtil.neitherNullNorEmpty(event)) {
            SubscriptionEntry entry = eventToPubSub.get(event);
            if (entry != null) {
                entry.removePublisher(producer);
            }
        }
    }

    public final void pairInput(final EventActor consumer, String event) {
        if (consumer != null && TextUtil.neitherNullNorEmpty(event)) {
            SubscriptionEntry entry = eventToPubSub.get(event);
            if (entry != null) {
                LOG.debug("{}[{}] is paired as a consumer of the event type '{}'", new Object[]{consumer.getType(), consumer.getId(), event});
                entry.addConsumer(consumer);
            }
        }
    }

    public final void unpairInput(final EventActor consumer, String event) {
        if (consumer != null && TextUtil.neitherNullNorEmpty(event)) {
            SubscriptionEntry entry = eventToPubSub.get(event);
            if (entry != null) {
                entry.removeSubscriber(consumer);
            }
        }
    }

    public final void enable(String actorId) throws ActorNotExistedException {
        if (TextUtil.neitherNullNorEmpty(actorId)) {
            if (!idToActor.containsKey(actorId)) {
                throw new ActorNotExistedException(actorId);
            } else {
                //idToActor.get(actorId).setDirty(!EventActorState.ENABLING.equals(idToActorState.get(actorId)));
                idToActorState.put(actorId, EventActorState.ENABLING);
            }
        }
    }

    public final void disable(String actorId) throws ActorNotExistedException {
        if (TextUtil.neitherNullNorEmpty(actorId)) {
            if (!idToActor.containsKey(actorId)) {
                throw new ActorNotExistedException(actorId);
            } else {
                //idToActor.get(actorId).setDirty(!EventActorState.FINISHED.equals(idToActorState.get(actorId)));
                idToActorState.put(actorId, EventActorState.FINISHED);
            }
        }
    }

    public Set<EventActor> getConsumers(String event) {
        if (TextUtil.neitherNullNorEmpty(event) && eventToPubSub != null) {
            SubscriptionEntry entry = eventToPubSub.get(event);
            if (entry != null) {
                return entry.getConsumers();
            }
        }
        return Collections.EMPTY_SET;
    }

    public Set<EventActor> getProducers(String event) {
        if (TextUtil.neitherNullNorEmpty(event) && eventToPubSub != null) {
            SubscriptionEntry entry = eventToPubSub.get(event);
            if (entry != null) {
                return entry.getProducers();
            }
        }
        return Collections.EMPTY_SET;
    }

    public Set<EventActor> getWorkingActors() {
        final Set<EventActor> excluded = new HashSet<>();
        SubscriptionEntry highPriorityEntries = eventToPubSub.get(EventType.EVENT_HIGH_PRIORITY_TYPE);
        if (highPriorityEntries != null && highPriorityEntries.getConsumers() != null) {
            excluded.addAll(highPriorityEntries.getConsumers());
        }
        SubscriptionEntry lowPriorityEntries = eventToPubSub.get(EventType.EVENT_LOW_PRIORITY_TYPE);
        if (lowPriorityEntries != null && lowPriorityEntries.getConsumers() != null) {
            excluded.addAll(lowPriorityEntries.getConsumers());
        }
        Map<String, EventActor> filteredActors = Maps.filterValues(idToActor, new Predicate<EventActor>() {
            @Override
            public boolean apply(@Nullable EventActor actor) {
                return actor != null && !excluded.contains(actor);
            }
        });
        if (filteredActors != null) {
            return new HashSet<>(filteredActors.values());
        } else {
            return Collections.EMPTY_SET;
        }

    }

    public Set<EventActor> getDirtyActors() {
        final Set<EventActor> excluded = new HashSet<>();
        /*
        SubscriptionEntry highPriorityEntries = eventToPubSub.get(EventType.EVENT_HIGH_PRIORITY_TYPE);
        if (highPriorityEntries != null && highPriorityEntries.getConsumers() != null) {
            excluded.addAll(highPriorityEntries.getConsumers());
        }
        SubscriptionEntry lowPriorityEntries = eventToPubSub.get(EventType.EVENT_LOW_PRIORITY_TYPE);
        if (lowPriorityEntries != null && lowPriorityEntries.getConsumers() != null) {
            excluded.addAll(lowPriorityEntries.getConsumers());
        }
        Map<String, EventActor> filteredActors = Maps.filterValues(idToActor, new Predicate<EventActor>() {
            @Override
            public boolean apply(@Nullable EventActor actor) {
                return actor != null && actor.isDirty() && !excluded.contains(actor);
            }
        });
        if (filteredActors != null) {
            return new HashSet<>(filteredActors.values());
        } else {
            return Collections.EMPTY_SET;
        }
        */
        return Collections.EMPTY_SET;
    }

    public void resetDirtyActors() {
        /*
        for (String id : idToActor.keySet()) {
            EventActor actor = idToActor.get(id);
            if (actor != null && actor.isDirty())
                actor.setDirty(false);
        }
        */
    }

    public Set<EventActor> getActors() {
        return new HashSet<>(idToActor.values());
    }

    public EventActor getActor(String actorId) {
        return idToActor.get(actorId);
    }

    public Set<String> getEvents() {
        return Collections.unmodifiableSet(eventToPubSub.keySet());
    }

    public Set<EventType> getWorkingEvents() {
        Set<EventType> workingEventTypes = new HashSet<>();
        for (String name : eventToPubSub.keySet()) {
            if (TextUtil.neitherNullNorEmpty(name)
                    && !EventType.EVENT_HIGH_PRIORITY_TYPE.equals(name)
                    && !EventType.EVENT_LOW_PRIORITY_TYPE.equals(name)) {
                SubscriptionEntry entry = eventToPubSub.get(name);
                if (entry != null && entry.getEventType() != null) {
                    workingEventTypes.add(entry.getEventType());
                }
            }
        }
        return workingEventTypes;
    }

    public Set<EventType> getDirtyEvents() {
        Set<EventType> dirtyEventTypes = new HashSet<>();
        for (String name : eventToPubSub.keySet()) {
            if (TextUtil.neitherNullNorEmpty(name)
                    && !EventType.EVENT_HIGH_PRIORITY_TYPE.equals(name)
                    && !EventType.EVENT_LOW_PRIORITY_TYPE.equals(name)) {
                SubscriptionEntry entry = eventToPubSub.get(name);
                if (entry != null
                        && entry.getEventType() != null
                        && entry.getEventType().isDirty()) {
                    dirtyEventTypes.add(entry.getEventType());
                }
            }
        }
        return dirtyEventTypes;
    }

    public void resetDirtyEvents() {
        for (String name : eventToPubSub.keySet()) {
            SubscriptionEntry entry = eventToPubSub.get(name);
            if (entry != null
                    && entry.getEventType() != null
                    && entry.getEventType().isDirty()) {
                entry.getEventType().setDirty(false);
            }
        }
    }


}