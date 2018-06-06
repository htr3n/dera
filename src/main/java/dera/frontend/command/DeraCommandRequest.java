package dera.frontend.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeraCommandRequest {

    protected DeraCommandType cmd;
    protected String elementType;
    protected String elementId;
    protected Set<String> input;
    protected Set<String> output;
    protected Set<String> trueEvent;
    protected Set<String> falseEvent;
    protected Set<String> attribute;

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public DeraCommandType getCmd() {
        return cmd;
    }

    public void setCmd(DeraCommandType cmd) {
        this.cmd = cmd;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public Set<String> getInput() {
        return input;
    }

    public void setInput(Set<String> input) {
        this.input = input;
    }

    public Set<String> getOutput() {
        return output;
    }

    public void setOutput(Set<String> output) {
        this.output = output;
    }

    public Set<String> getTrueEvent() {
        return trueEvent;
    }

    public void setTrueEvent(Set<String> trueEvent) {
        this.trueEvent = trueEvent;
    }

    public Set<String> getFalseEvent() {
        return falseEvent;
    }

    public void setFalseEvent(Set<String> falseEvent) {
        this.falseEvent = falseEvent;
    }
}
