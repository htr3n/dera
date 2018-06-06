package dera.frontend.mapping;

import dera.core.*;
import dera.runtime.Application;
import dera.runtime.ApplicationInstance;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joda.time.DateTime;

import java.util.Set;

public class MixInModule extends SimpleModule {

    public MixInModule() {
        super("MixInModule", new Version(0, 0, 1, null, null, null));
    }

    @Override
    public void setupModule(SetupContext context) {
        context.setMixInAnnotations(Condition.class, ConditionMixIn.class);
        context.setMixInAnnotations(Action.class, ActorMixIn.class);
        context.setMixInAnnotations(EventType.class, EventTypeMixIn.class);
        context.setMixInAnnotations(Application.class, ApplicationMixIn.class);
        context.setMixInAnnotations(ApplicationInstance.class, ApplicationInstanceMixIn.class);
    }


    interface ActorMixIn {
        @JsonIgnore
        public ExecutionDomain getDomain();

        @JsonIgnore
        public Behavior getBehavior();

        /*
        @JsonIgnore
        public Map<String, String> getAttributes();
        */

        @JsonIgnore
        public boolean isDirty();

        @JsonIgnore
        public void setDirty(boolean dirty);

        @JsonIgnore
        public Predicate getPredicate();

        @JsonProperty("input")
        public Set<String> getInput();

        @JsonProperty("output")
        public Set<String> getOutput();

    }

    interface EventTypeMixIn {

        @JsonIgnore
        public ExecutionDomain getDomain();

        @JsonIgnore
        public boolean isDirty();

        @JsonIgnore
        public void setDirty(boolean dirty);

        @JsonIgnore
        public boolean match(EventType other);
    }

    interface ConditionMixIn {
        @JsonIgnore
        public ExecutionDomain getDomain();

        @JsonIgnore
        public Behavior getBehavior();

        @JsonIgnore
        public Set<String> getOutput();

        /*
        @JsonIgnore
        public Map<String, String> getAttributes();
        */

        @JsonIgnore
        public boolean isDirty();

        @JsonIgnore
        public void setDirty(boolean dirty);

        @JsonProperty("input")
        public Set<String> getInput();

        @JsonProperty("trueEvent")
        public Set<String> getTrueEvent();

        @JsonProperty("falseEvent")
        public Set<String> getFalseEvent();

        @JsonIgnore
        public Predicate getPredicate();
    }

    interface ApplicationMixIn {
    }

    interface ApplicationInstanceMixIn {
        @JsonSerialize(using = JodaTimeSerializer.class)
        public DateTime getStartTime();

        @JsonSerialize(using = JodaTimeSerializer.class)
        public DateTime getFinishTime();

    }

}