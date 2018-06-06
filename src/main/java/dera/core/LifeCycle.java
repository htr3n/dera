package dera.core;

public abstract class LifeCycle  {

    protected boolean started = false;
    protected boolean stopped = true;

    public boolean isStarted() {
        return started;
    }

    public boolean isStopped() {
        return stopped;
    }

    protected void switchState(){
        started = stopped;
        stopped = started;
    }

    public abstract void init();

    public abstract void start();

    public abstract void stop();

}
