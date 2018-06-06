package dera.core;

import dera.error.ActorExistedException;
import dera.error.ActorNotExistedException;
import dera.error.EventTypeExistedException;
import dera.error.EventTypeNotExistedException;
import dera.runtime.Application;

import java.util.Map;

public interface ExecutionDomain extends Namespace {

    void addEvent(EventType eventType) throws EventTypeExistedException;

    void addEvent(String event) throws EventTypeNotExistedException, EventTypeExistedException;

    void removeEvent(String event) throws EventTypeNotExistedException;

    EventType getEvent(String event);

    void register(EventActor action) throws ActorExistedException;

    void deregister(String actorId) throws ActorNotExistedException;

    void disable(String actorId) throws ActorNotExistedException;

    void enable(String actorId) throws ActorNotExistedException;

    void bindInput(EventActor consumer, String eventId);

    void unbindInput(EventActor consumer, String eventId);

    void bindOutput(EventActor producer, String eventId);

    void unbindOutput(EventActor producer, String eventId);

    void execute(Event instance, EventActor action);

    void publish(String eventId, String correlationId, Map<String, String> attributes);

    void publish(String event);

    void publish(Event instance);

    void addApplication(Application application);

    void removeApplication(String appId);
}
