package dera.runtime;

import dera.core.*;
import dera.error.ActorExistedException;
import dera.error.ActorNotExistedException;
import dera.util.TextUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationManager extends LifeCycle {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationManager.class);

    private Map<String, Application> idToApplication; // for fast querying the apps
    private Map<String, ApplicationInstance> idToInstance; // for fast querying the instances
    private Map<String, Set<String>> startEventToAppId;
    private Map<String, Set<String>> endEventToAppId;
    private Map<String, Set<ApplicationInstance>> appIdToInstance;
    private ApplicationMonitor monitor;
    private final ExecutionDomain domain;

    public ApplicationManager(final ExecutionDomain domain) {
        this.domain = domain;
    }

    @Override
    public void init() {
        monitor = new ApplicationMonitor(domain);
        idToApplication = new ConcurrentHashMap<>();
        idToInstance = new ConcurrentHashMap<>();
        startEventToAppId = new ConcurrentHashMap<>();
        endEventToAppId = new ConcurrentHashMap<>();
        appIdToInstance = new ConcurrentHashMap<>();
    }

    @Override
    public void start() {
        switchState();
    }

    @Override
    public void stop() {
        try {
            domain.deregister(monitor.getType());
        } catch (ActorNotExistedException e) {
        }
        switchState();
    }


    public boolean addApplication(final Application app) {
        if (app != null) {
            String appId = app.getId();
            if (idToApplication.get(appId) != null) {
                LOG.warn("There exists an application with ID {}. Ignored!", new Object[]{appId});
                return false;
            }
            idToApplication.put(app.getId(), app);
            LOG.info("Added {}", new Object[]{app});
            for (String eventId : app.getStart()) {
                Set<String> appIds = startEventToAppId.get(eventId);
                if (appIds == null) { // first time?
                    appIds = new HashSet<>();
                    startEventToAppId.put(eventId, appIds);
                    //domain.bindInput(monitor, eventId);
                }
                appIds.add(app.getId());
            }
            for (String eventId : app.getEnd()) {
                Set<String> appIds = endEventToAppId.get(eventId);
                if (appIds == null) { // first time?
                    appIds = new HashSet<>();
                    endEventToAppId.put(eventId, appIds);
                    //domain.bindInput(monitor, eventId);
                }
                appIds.add(app.getId());
            }
        }
        return true;
    }

    public void deleteApplication(final String appId) {
        if (TextUtil.neitherNullNorEmpty(appId)) {
            appIdToInstance.remove(appId);
            idToApplication.remove(appId);
        }
    }

    public Set<Application> getApplications() {
        return new HashSet<>(idToApplication.values());
    }

    public Set<ApplicationInstance> getInstances(final String appId) {
        if (appId != null) {
            return appIdToInstance.get(appId);
        }
        return Collections.<ApplicationInstance>emptySet();
    }

    public Application getApplication(final String appId) {
        return idToApplication.get(appId);
    }

    private void createInstance(final String appId, final String correlationId) {
        if (TextUtil.neitherNullNorEmpty(appId) && TextUtil.neitherNullNorEmpty(correlationId)) {

            final Application app = idToApplication.get(appId);

            if (app != null) {
                final ApplicationInstance instance = app.createInstance(correlationId);
                instance.setState(ApplicationInstanceState.RUNNING);
                instance.setStartTime(new DateTime());
                Set<ApplicationInstance> instances = appIdToInstance.get(appId);
                if (instances == null) { // first time?
                    instances = new HashSet<>();
                    appIdToInstance.put(appId, instances);
                }
                instances.add(instance);
                idToInstance.put(instance.getId(), instance);
                LOG.info("Application '{}' => new instance '{}/{}'", new Object[]{appId, instance.getId(), instance.getCorrelationId()});
            }
        }
    }

    public Set<Application> getApps() {
        return new HashSet<Application>(idToApplication.values());
    }

    class ApplicationMonitor extends Action {

        ApplicationMonitor(final ExecutionDomain domain) {
            super("ApplicationMonitor");
            if (domain != null) {
                try {
                    domain.register(this);
                    super.addInput(EventType.EVENT_HIGH_PRIORITY_TYPE);
                } catch (ActorExistedException e) {
                }
            }
        }

        @Override
        public void notified(final Event incoming) {
            if (incoming == null) {
                return;
            }
            LOG.debug("Received {}", new Object[]{incoming});
            final String correlationId = incoming.getCorrelationId();
            final String event = incoming.getType();

            if (matchStart(event)) {
                final Set<String> matchedAppIds = startEventToAppId.get(event);
                if (matchedAppIds != null && !matchedAppIds.isEmpty()) {
                    for (String appId : matchedAppIds) {
                        // for each application, create a new instance
                        try {
                            createInstance(appId, correlationId);
                        } catch (Exception e) {
                            LOG.debug("Error: ", e);
                        }
                    }
                }
            } else if (matchEnd(event)) {
                final Set<String> matchedAppIds = endEventToAppId.get(event);
                if (matchedAppIds != null && !matchedAppIds.isEmpty()) {
                    for (String appId : matchedAppIds) {
                        final Set<ApplicationInstance> instances = appIdToInstance.get(appId);
                        if (instances != null) {
                            for (final ApplicationInstance instance : instances) {
                                if (instance != null && instance.getState() == ApplicationInstanceState.RUNNING) {
                                    instance.setState(ApplicationInstanceState.COMPLETED);
                                    instance.setFinishTime(new DateTime());
                                    LOG.info("The instance '{}' of the application '{}' is completed",
                                            new Object[]{instance.getId(), appId});
                                }
                            }
                        }
                    }
                }
            }
        }

        private boolean matchStart(final String event) {
            return startEventToAppId != null
                    && startEventToAppId.keySet() != null
                    && startEventToAppId.keySet().contains(event);
        }

        private boolean matchEnd(final String event) {
            return endEventToAppId != null
                    && endEventToAppId.keySet() != null
                    && endEventToAppId.keySet().contains(event);
        }

        @Override
        public final void addInput(String... events) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final void addOutput(String... events) {
            throw new UnsupportedOperationException();
        }
    }
}
