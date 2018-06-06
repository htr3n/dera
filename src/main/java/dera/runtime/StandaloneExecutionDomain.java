package dera.runtime;

import dera.core.*;
import dera.error.ActorExistedException;
import dera.error.ActorNotExistedException;
import dera.error.EventTypeExistedException;
import dera.error.EventTypeNotExistedException;
import dera.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StandaloneExecutionDomain extends LifeCycle implements ExecutionDomain, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(StandaloneExecutionDomain.class);
    private static final int DAEMON_POOL_SIZE = 4;
    private static final StandaloneExecutionDomain INSTANCE = new StandaloneExecutionDomain();
    private final String id = TextUtil.randomId("domain");
    private String namespaceUri;
    private EventChannel eventChannel;
    private DomainServer domainServer;
    private ExecutionManager executionManager;
    private EventActorManager eventActorManager;
    private ApplicationManager applicationManager;
    private ExecutorService daemonExecutor;
    private DomainConfiguration configuration;

    private StandaloneExecutionDomain() {
        daemonExecutor = Executors.newFixedThreadPool(DAEMON_POOL_SIZE, new DaemonThreadFactory());
        init();
    }

    public static StandaloneExecutionDomain getInstance() {
        return INSTANCE;
    }

    public String getId() {
        return id;
    }

    public DomainConfiguration getConfiguration() {
        return configuration;
    }

    public ExecutorService getDaemonExecutor() {
        return daemonExecutor;
    }

    public EventActorManager getEventActorManager() {
        return eventActorManager;
    }

    public ExecutionManager getExecutionManager() {
        return executionManager;
    }

    public EventChannel getEventChannel() {
        return eventChannel;
    }

    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }

    @Override
    public void init() {
        configuration = new DomainConfiguration();
        eventChannel = new EventChannel(this);
        eventChannel.init();
        executionManager = new ExecutionManager(this);
        executionManager.init();
        eventActorManager = new EventActorManager(this);
        eventActorManager.init();
        applicationManager = new ApplicationManager(this);
        applicationManager.init();
        domainServer = new DomainServer(this);
        domainServer.init();
    }

    @Override
    public void start() {
        eventChannel.start();
        executionManager.start();
        eventActorManager.start();
        applicationManager.start();
        domainServer.start();
        LOG.info("The {} is up and running", new Object[]{this});
        switchState();
    }

    @Override
    public void stop() {
        if (domainServer != null && !domainServer.isStopped())
            domainServer.stop();
        if (eventChannel != null && eventChannel.isStopped())
            eventChannel.stop();
        if (executionManager != null && executionManager.isStopped())
            executionManager.stop();
        if (eventActorManager != null && eventActorManager.isStopped())
            eventActorManager.stop();
        if (applicationManager != null && !applicationManager.isStopped())
            applicationManager.stop();
        daemonExecutor.shutdownNow();
        LOG.info("The {} is stopped", new Object[]{this});
        switchState();
    }

    @Override
    public void addEvent(String event) throws EventTypeNotExistedException, EventTypeExistedException {
        LOG.info("Add event type '{}'", new Object[]{event});
        eventActorManager.addEvent(event);
    }

    @Override
    public void addEvent(EventType eventType) throws EventTypeExistedException {
        LOG.info("Adding event types '{}'", new Object[]{eventType});
        eventActorManager.addEvent(eventType);
    }

    @Override
    public void removeEvent(String event) throws EventTypeNotExistedException {
        LOG.info("Remove event type '{}'", new Object[]{event});
        eventActorManager.removeEvent(event);
    }

    @Override
    public EventType getEvent(String event) {
        return eventActorManager.getEvent(event);
    }

    @Override
    public void register(final EventActor action) throws ActorExistedException {
        LOG.info("Register event actor '{}'", new Object[]{action});
        eventActorManager.register(action);
    }

    @Override
    public void deregister(String actorId) throws ActorNotExistedException {
        LOG.info("Deregister event actor '{}'", new Object[]{actorId});
        eventActorManager.deregister(actorId);
    }

    @Override
    public void disable(String actorId) throws ActorNotExistedException {
        LOG.info("Disable event actor '{}'", new Object[]{actorId});
        eventActorManager.disable(actorId);
    }

    @Override
    public void enable(String actorId) throws ActorNotExistedException {
        LOG.info("Enable event actor '{}'", new Object[]{actorId});
        eventActorManager.enable(actorId);
    }

    @Override
    public void bindInput(final EventActor consumer, String event) {
        LOG.debug("Paired input {} <--> {}", new Object[]{consumer, event});
        eventActorManager.pairInput(consumer, event);
    }

    @Override
    public void bindOutput(final EventActor producer, String event) {
        LOG.debug("Paired output {} <--> {}", new Object[]{producer, event});
        eventActorManager.pairOutput(producer, event);
    }

    @Override
    public void unbindOutput(EventActor producer, String event) {
        LOG.debug("Unpaired output {} <--> {}", new Object[]{producer, event});
        eventActorManager.unpairOutput(producer, event);
    }

    @Override
    public void unbindInput(EventActor consumer, String event) {
        LOG.debug("Unpaired input {} <--> {}", new Object[]{consumer, event});
        eventActorManager.unpairInput(consumer, event);
    }

    @Override
    public void execute(final Event instance, final EventActor action) {
        executionManager.submitTask(instance, action);
    }

    @Override
    public void publish(String eventId, String correlationId, final Map<String, String> attributes) {
        LOG.debug("Publish event type '{}' with correlation ID = '{}'", new Object[]{eventId, correlationId});
        EventType type = eventActorManager.getEvent(eventId);
        if (type != null) {
            final Event eventInstance = type.newInstance();
            eventInstance.setCorrelationId(correlationId);
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                try {
                    eventInstance.setAttribute(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                }
            }
            eventChannel.send(eventInstance);
        } else {
            LOG.error("Unable to publish the event of non-existed type {}", new Object[]{eventId});
        }
    }

    @Override
    public void publish(String event) {
        LOG.debug("Publish event type '{}'", new Object[]{event});
        publish(event, TextUtil.randomFixedLengthString(), Collections.EMPTY_MAP);
    }

    @Override
    public void publish(Event instance) {
        LOG.debug("Publish event instance '{}'", new Object[]{instance});
        eventChannel.send(instance);
    }

    @Override
    public void addApplication(final Application application) {
        LOG.debug("Add application '{}'", new Object[]{application});
        applicationManager.addApplication(application);
    }

    @Override
    public void removeApplication(String appId) {
        LOG.debug("Remove application '{}'", new Object[]{appId});
        applicationManager.deleteApplication(appId);
    }

    @Override
    public String toString() {
        return new StringBuffer()
                .append(getClass().getSimpleName())
                .append(" [")
                .append(id)
                .append("] ")
                .toString();
    }

    @Override
    public String getUri() {
        return namespaceUri;
    }

    @Override
    public void setUri(String uri) {
        this.namespaceUri = uri;
    }

    @Override
    public void run() {
        start();
    }

    public static void main(String[] args) {
        final CountDownLatch latch = new CountDownLatch(1);
        final StandaloneExecutionDomain domain = StandaloneExecutionDomain.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
                domain.stop();
            }
        }));
        domain.init();
        domain.getDaemonExecutor().execute(domain);
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
    }

}
