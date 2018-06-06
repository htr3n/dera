package dera.runtime;

import org.joda.time.DateTime;

public class ApplicationInstance {

    private final Application application;
    private final String correlationId;

    private String id;
    private DateTime startTime;
    private DateTime finishTime;
    private ApplicationInstanceState state;

    public ApplicationInstance(final Application application, final String correlationId) {
        this.correlationId = correlationId;
        this.application = application;
    }

    public ApplicationInstanceState getState() {
        return state;
    }

    public void setState(ApplicationInstanceState state) {
        this.state = state;
    }

    public Application getApplication() {
        return application;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(DateTime finishTime) {
        this.finishTime = finishTime;
    }

    @Override
    public String toString() {
        return new StringBuffer(getClass().getSimpleName())
                .append("[id=")
                .append(id)
                .append(" correlationId=")
                .append(correlationId)
                .append(" state=")
                .append(state)
                .append(" start=")
                .append(startTime != null ? startTime : "")
                .append(" finish=")
                .append(finishTime != null ? finishTime : "")
                .append("]")
                .toString();
    }
}
