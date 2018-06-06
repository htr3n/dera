package dera.core;

import java.util.Set;

public interface EventActor {

    String getId();

    String getType();

    Set<String> getInput();

    Set<String> getOutput();

    Behavior getBehavior();

    void setBehavior(Behavior behavior);

    ExecutionDomain getDomain();

    void setDomain(ExecutionDomain domain);

    void setObjectReference(Set<ObjectReference> objectReference);

    Set<ObjectReference> getObjectReference();

    void notified(Event incomingEvent);

    void fire(Event incomingEvent);

}
