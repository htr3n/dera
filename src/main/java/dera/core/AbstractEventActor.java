package dera.core;

import dera.util.CollectionUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractEventActor implements EventActor {

    protected String id;
    protected String type;
    protected Behavior behavior;
    protected Set<String> input;
    protected final Set<ObjectReference> objectReference;
    protected ExecutionDomain domain;

    public AbstractEventActor(String id) {
        this.id = id;
        this.type = this.getClass().getSimpleName();
        this.input = Collections.synchronizedSet(new HashSet<String>());
        this.objectReference = Collections.synchronizedSet(new HashSet<ObjectReference>());
    }

    @Override
    public Set<String> getInput() {
        if (this.input != null)
            return this.input;
        else
            return Collections.<String>emptySet();
    }

    @Override
    public Set<String> getOutput() {
        return Collections.<String>emptySet();
    }

    @Override
    public Behavior getBehavior() {
        return this.behavior;
    }

    @Override
    public void setBehavior(Behavior behavior) {
        if (behavior != null) {
            this.behavior = behavior;
            behavior.setOwner(this);
        }
    }

    @Override
    public ExecutionDomain getDomain() {
        return this.domain;
    }

    @Override
    public void setDomain(ExecutionDomain domain) {
        this.domain = domain;
    }

    @Override
    public void setObjectReference(Set<ObjectReference> objectReference) {
        this.objectReference.addAll(objectReference);
    }

    @Override
    public Set<ObjectReference> getObjectReference() {
        return this.objectReference;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }


    public abstract void fire(final Event incomingEvent);

    public abstract void notified(Event incomingEvent);

    protected final void publish(String correlationId, Map<String, String> attributes, String... events) {
        if (this.domain != null && CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                this.domain.publish(event, correlationId, attributes);
            }
        }
    }

    protected final void publish(String correlationId, Map<String, String> attributes, Set<String> events) {
        if (this.domain != null && CollectionUtil.neitherNullNorEmpty(events)) {
            for (String event : events) {
                this.domain.publish(event, correlationId, attributes);
            }
        }
    }

}
