package dera.frontend.command;

import dera.DERA;
import dera.core.Action;
import dera.core.Barrier;
import dera.core.Condition;
import dera.core.ExecutionDomain;
import dera.error.ActorExistedException;
import dera.error.ActorNotExistedException;
import dera.runtime.StandaloneExecutionDomain;
import dera.util.CollectionUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ActorCommandRequest implements Command {

    protected String type;
    protected ActorCommandKind command;
    protected String id;
    protected Set<String> input;
    protected Set<String> output;
    protected Set<String> trueEvent;
    protected Set<String> falseEvent;

    public ActorCommandRequest() {
    }

    public ActorCommandRequest(final Action actor) {
        if (actor != null) {
            this.id = actor.getId();
            this.input = new HashSet<>(actor.getInput());
            this.output = new HashSet<>(actor.getOutput());
        }
    }

    public ActorCommandRequest(final Barrier barrier) {
        if (barrier != null) {
            this.id = barrier.getId();
            this.input = new HashSet<>(barrier.getInput());
            this.output = new HashSet<>(barrier.getOutput());
        }
    }

    public ActorCommandRequest(final Condition condition) {
        if (condition != null) {
            this.id = condition.getId();
            this.input = new HashSet<>(condition.getInput());
            this.falseEvent = new HashSet<>(condition.getFalseEvent());
            this.trueEvent = new HashSet<>(condition.getTrueEvent());
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ActorCommandKind getCommand() {
        return command;
    }

    public void setCommand(ActorCommandKind command) {
        this.command = command;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public void execute() throws ActorNotExistedException, ActorExistedException {
        switch (command) {
            case add:
                createAndAddActor();
                break;
            case remove:
                StandaloneExecutionDomain.getInstance().deregister(id);
                break;
            case enable:
                StandaloneExecutionDomain.getInstance().enable(id);
                break;
            case disable:
                StandaloneExecutionDomain.getInstance().disable(id);
                break;
            default:
                break;
        }
    }

    @Override
    public void undo() {
    }

    protected Action createAndAddActor() throws ActorExistedException {
        ExecutionDomain domain = StandaloneExecutionDomain.getInstance();
        switch (type) {
            case "Barrier":
                Barrier barrier = DERA.newBarrier(id, domain);
                if (CollectionUtil.neitherNullNorEmpty(input)) {
                    barrier.addInput(input.toArray(new String[input.size()]));
                }
                if (CollectionUtil.neitherNullNorEmpty(output)) {
                    barrier.addOutput(output.toArray(new String[output.size()]));
                }
                return barrier;
            case "Condition":
                Condition condition = DERA.newCondition(id, domain);
                if (CollectionUtil.neitherNullNorEmpty(input)) {
                    condition.addInput(input.toArray(new String[input.size()]));
                }
                if (CollectionUtil.neitherNullNorEmpty(trueEvent)) {
                    condition.addTrueEvent(trueEvent.toArray(new String[trueEvent.size()]));
                }
                if (CollectionUtil.neitherNullNorEmpty(falseEvent)) {
                    condition.addFalseEvent(falseEvent.toArray(new String[falseEvent.size()]));
                }
                return condition;
            case "EventActor":
                Action actor = DERA.newAction(id, domain);
                if (CollectionUtil.neitherNullNorEmpty(input)) {
                    actor.addInput(input.toArray(new String[input.size()]));
                }
                if (CollectionUtil.neitherNullNorEmpty(output)) {
                    actor.addOutput(output.toArray(new String[output.size()]));
                }
                return actor;
            default:
                break;
        }
        return null;
    }
}

