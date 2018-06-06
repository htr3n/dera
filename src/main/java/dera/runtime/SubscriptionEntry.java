package dera.runtime;

import dera.core.EventActor;
import dera.core.EventType;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionEntry {

    private EventType eventType;
    private Set<EventActor> consumers;
    private Set<EventActor> producers;

    public SubscriptionEntry(EventType eventType) {
        this.eventType = eventType;
    }

    public Set<EventActor> getConsumers() {
        return consumers;
    }

    public void setConsumers(Set<EventActor> consumers) {
        this.consumers = consumers;
    }

    public Set<EventActor> getProducers() {
        return producers;
    }

    public void setProducers(Set<EventActor> producers) {
        this.producers = producers;
    }

    public void addConsumer(EventActor consumer) {
        if (consumer == null)
            return;
        if (this.consumers == null) // laziness
            this.consumers = Collections.newSetFromMap(new ConcurrentHashMap<EventActor, Boolean>());
        this.consumers.add(consumer);
    }

    public void addProducer(EventActor producer) {
        if (producer == null)
            return;
        if (this.producers == null) // laziness
            this.producers = Collections.newSetFromMap(new ConcurrentHashMap<EventActor, Boolean>());
        this.producers.add(producer);
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public void removePublisher(EventActor producer) {
        if (producer == null || this.producers == null)
            return;
        this.producers.remove(producer);
    }

    public void removeSubscriber(EventActor consumer) {
        if (consumer == null || this.consumers == null)
            return;
        this.consumers.remove(consumer);
    }

    @Override
    public String toString() {
        return "SubscriptionEntry{" +
                "eventType=" + eventType +
                ", consumers=" + consumers +
                ", producers=" + producers +
                '}';
    }
}
