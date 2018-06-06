package dera.frontend.websocket;

import dera.core.EventType;
import dera.frontend.command.DirtyCommandResponse;
import dera.frontend.mapping.ObjectToJSON;
import dera.runtime.StandaloneExecutionDomain;
import dera.util.CollectionUtil;
import dera.util.JacksonUtil;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketMonitorServlet extends WebSocketServlet {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketMonitorServlet.class);
    private static final ObjectToJSON OBJECT_TO_JSON_MAPPER = JacksonUtil.getObjectToJsonMapper();

    private final Set<WebSocketMonitor> listeners = new CopyOnWriteArraySet<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final StandaloneExecutionDomain domain = StandaloneExecutionDomain.getInstance();

    private static final long TASK_INITIAL_DELAY_IN_MILLISECONDS = 5;
    private static final long TASK_PERIOD_IN_MILLISECONDS = 5;

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator(new MonitorWebSocketCreator(this));
    }

    @Override
    public void init() throws ServletException {
        super.init();
        LOG.debug("Start the monitoring server");
        executor.scheduleAtFixedRate(new DomainStatusSender(), TASK_INITIAL_DELAY_IN_MILLISECONDS, TASK_PERIOD_IN_MILLISECONDS, TimeUnit.SECONDS);
    }

    private DirtyCommandResponse checkDirtyActors() {
        //Set<EventActor> dirtyActors = domain.getEventActorManager().getDirtyActors();
        DirtyCommandResponse result = null;
        /*
        if (CollectionUtil.neitherNullNorEmpty(dirtyActors)) {
            result = new DirtyCommandResponse();
            result.setType(Action.class.getSimpleName());
            result.setData(dirtyActors);
        }
        */
        return result;
    }

    private DirtyCommandResponse checkDirtyApplications() {
        //Set<Application> dirtyApps = domain.getApplicationManager().getDirtyApps();
        DirtyCommandResponse result = null;
        /*
        if (CollectionUtil.neitherNullNorEmpty(dirtyApps)) {
            result = new DirtyCommandResponse();
            result.setType(Application.class.getSimpleName());
            result.setData(dirtyApps);
        }
        */
        return result;
    }

    private DirtyCommandResponse checkDirtyEvents() {
        Set<EventType> dirtyEventTypes = domain.getEventActorManager().getDirtyEvents();
        DirtyCommandResponse result = null;
        if (CollectionUtil.neitherNullNorEmpty(dirtyEventTypes)) {
            result = new DirtyCommandResponse();
            result.setType(EventType.class.getSimpleName());
            result.setData(dirtyEventTypes);
        }
        return result;
    }


    public Set<WebSocketMonitor> getListeners() {
        return listeners;
    }

    public void addListener(WebSocketMonitor listener) {
        listeners.add(listener);
    }

    public void removeListener(WebSocketMonitor listener) {
        listeners.remove(listener);
    }

    class DomainStatusSender implements Runnable {
        @Override
        public void run() {
            for (WebSocketMonitor client : getListeners()) {
                if (client.isOpen()) {
                    try {
                        if (domain != null) {
                            DirtyCommandResponse response = checkDirtyActors();
                            if (response != null)
                                client.sendMessage(OBJECT_TO_JSON_MAPPER.convert(response, "UTF-8"));

                            response = checkDirtyEvents();
                            if (response != null)
                                client.sendMessage(OBJECT_TO_JSON_MAPPER.convert(response, "UTF-8"));

                            response = checkDirtyApplications();
                            if (response != null)
                                client.sendMessage(OBJECT_TO_JSON_MAPPER.convert(response, "UTF-8"));

                        }
                    } catch (Exception e) {
                        LOG.error("Error: ", e);
                    }
                }
            }
        }
    }
}
