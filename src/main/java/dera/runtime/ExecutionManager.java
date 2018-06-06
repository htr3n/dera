package dera.runtime;

import dera.core.EventActor;
import dera.core.Event;
import dera.core.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

public class ExecutionManager extends LifeCycle {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionManager.class);
    private BoundedBlockingExecutor executor;
    private StandaloneExecutionDomain domain;
    private static final long MONITORING_INTERVAL_IN_MILLISECONDS = 5000L;

    public ExecutionManager(StandaloneExecutionDomain domain) {
        this.domain = domain;
    }

    @Override
    public void init() {
    }

    @Override
    public void start() {
        executor = new BoundedBlockingExecutor(domain.getConfiguration().getMaxPoolSize());
        if (domain.getConfiguration().isMonitored()) {
            startMonitoring();
        }
        LOG.info("Started the Execution Manager with the pool size [{}]", new Object[]{domain.getConfiguration().getMaxPoolSize()});
        switchState();
    }

    @Override
    public void stop() {
        try {
            LOG.debug("Shutting down the executor rest used for executing actors");
            if (executor != null && !(executor.isShutdown() || executor.isTerminating() || executor.isTerminated()))
                executor.shutdownNow();
            LOG.info("The execution manager is stopped");
            switchState();
        } catch (SecurityException e) {
            LOG.debug("Error occurred when shutting down the execution manager: ", e);
        }
    }

    public void submitTask(final Event instance, final EventActor actor) {
        if (instance != null && actor != null) {
            executor.execute(new ExecutableEntry(instance, actor));
        }
    }

    private void startMonitoring() {
        domain.getDaemonExecutor().execute(new ExecutionMonitor(executor, MONITORING_INTERVAL_IN_MILLISECONDS));
    }

    static class ExecutionMonitor implements Runnable {

        private final ThreadPoolExecutor executor;
        private final long frequency;

        public ExecutionMonitor(final ThreadPoolExecutor executor, final long frequency) {
            this.executor = executor;
            this.frequency = frequency;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    LOG.info(
                            "[execution-monitor] [{}/{}] Active: {}, Completed: {}, Total: {}, isShutdown: {}, isTerminated: {}",
                            new Object[]{
                                    executor.getPoolSize(),
                                    executor.getCorePoolSize(),
                                    executor.getActiveCount(),
                                    executor.getCompletedTaskCount(),
                                    executor.getTaskCount(),
                                    executor.isShutdown(),
                                    executor.isTerminated()});
                    Thread.sleep(frequency);
                }
            } catch (Exception e) {
                LOG.error("Error during execution monitoring.", e);
            }
        }
    }
}
