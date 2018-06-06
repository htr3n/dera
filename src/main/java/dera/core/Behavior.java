package dera.core;

public abstract class Behavior {

    protected EventActor owner;

    public EventActor getOwner() {
        return owner;
    }

    public void setOwner(final EventActor owner) {
        this.owner = owner;
    }

    public void calledBeforeExecution(final Event incomingEvent) {
    }

    public void calledAfterExecution(final Event incomingEvent) {
    }

    public abstract void run(final Event instance);

}
