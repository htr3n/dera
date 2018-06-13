package dera.frontend.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import dera.DERA;
import dera.error.EventTypeExistedException;
import dera.error.EventTypeNotExistedException;
import dera.runtime.StandaloneExecutionDomain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventCommandRequest implements Command {

    @JsonProperty(required = true)
    protected EventCommandKind cmd;

    @JsonProperty(required = true)
    private String type;

    @JsonProperty(required = true)
    private String elementId;

    private Set<String> attributes;

    public EventCommandRequest() {
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EventCommandKind getCmd() {
        return cmd;
    }

    public void setCmd(EventCommandKind cmd) {
        this.cmd = cmd;
    }

    public Set<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public void execute() throws EventTypeNotExistedException, EventTypeExistedException {
        switch (cmd) {
            case add:
                String[] eventAttributes = attributes != null ? attributes.toArray(new String[attributes.size()]) : null;
                DERA.newEventType(elementId, eventAttributes, null, StandaloneExecutionDomain.getInstance());
                break;
            case remove:
                StandaloneExecutionDomain.getInstance().removeEvent(elementId);
                break;
            default:
                break;
        }
    }

    @Override
    public void undo() {
    }
}
