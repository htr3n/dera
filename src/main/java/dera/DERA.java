package dera;

import dera.error.ActorExistedException;
import dera.error.EventTypeExistedException;
import dera.extension.EventLogger;
import dera.extension.Timer;
import dera.extension.SimpleEventBridge;
import dera.extension.Trigger;
import dera.runtime.Application;
import dera.core.*;

import java.util.concurrent.TimeUnit;

public final class DERA {

    private DERA() {
    }

    public static EventType newEventType(String event) throws EventTypeExistedException {
        return newEventType(event, null, null, null);
    }

    public static EventType newEventType(String event, ExecutionDomain domain) throws EventTypeExistedException {
        return newEventType(event, null, null, domain);
    }

    public static EventType newEventType(String event, String[] attributes, EventType[] templates, ExecutionDomain domain) throws EventTypeExistedException {
        EventType type = new EventType(event, attributes, templates);
        if (domain != null) {
            domain.addEvent(type);
        }
        return type;
    }

    public static Action newAction(String id, final ExecutionDomain domain) throws ActorExistedException {
        Action actor = new Action(id);
        if (domain != null) {
            domain.register(actor);
        }
        return actor;
    }

    public static Barrier newBarrier(String id, final ExecutionDomain domain) throws ActorExistedException {
        Barrier actor = new Barrier(id);
        if (domain != null) {
            domain.register(actor);
        }
        return actor;
    }

    public static Condition newCondition(String id, final ExecutionDomain domain) throws ActorExistedException {
        Condition actor = new Condition(id);
        if (domain != null) {
            domain.register(actor);
        }
        return actor;
    }

    public static Trigger newTrigger(String id, final ExecutionDomain domain) throws ActorExistedException {
        Trigger actor = new Trigger(id);
        if (domain != null) {
            domain.register(actor);
        }
        return actor;
    }

    public static Timer newTimer(String id, long initialDelay, long interval, TimeUnit timeUnit, final ExecutionDomain domain) throws ActorExistedException {
        Timer actor = new Timer(id, initialDelay, interval, timeUnit);
        if (domain != null) {
            domain.register(actor);
        }
        return actor;
    }

    public static EventLogger newEventLogger(ExecutionDomain domain) throws ActorExistedException {
        EventLogger actor = new EventLogger();
        if (domain != null) {
            domain.register(actor);
            actor.addInput(EventType.EVENT_LOW_PRIORITY_TYPE);
        }
        return actor;
    }

    public static Bridge newEventBridge(String id, String host, int port, boolean secure, final ExecutionDomain domain) throws ActorExistedException {
        Bridge actor = new SimpleEventBridge(id, host, port, secure);
        if (domain != null) {
            domain.register(actor);
        }
        return actor;
    }

    public static Application newApplication(String appId) {
        return new Application(appId);
    }
}