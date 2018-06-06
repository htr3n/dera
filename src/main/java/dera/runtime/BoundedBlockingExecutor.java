package dera.runtime;

import dera.core.EventActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

class BoundedBlockingExecutor extends ThreadPoolExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(BoundedBlockingExecutor.class);
    private static final double TO_MILLISECOND = 1000000.0;
    private static final int CORE_POOL_SIZE = 0;
    private static final long KEEP_ALIVE_TIME = 60L;
    private final Semaphore semaphore;

    private final ThreadLocal<Long> startTime = new ThreadLocal<>();
    private final AtomicLong numTasks = new AtomicLong();
    private final AtomicLong totalTime = new AtomicLong();

    public BoundedBlockingExecutor(int maxPoolSize) {
        super(CORE_POOL_SIZE, maxPoolSize, KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        this.semaphore = new Semaphore(maxPoolSize);
    }

    @Override
    public void execute(Runnable task) {
        boolean acquired = false;
        do {
            try {
                semaphore.acquire();
                acquired = true;
            } catch (InterruptedException e) {
            }
        } while (!acquired);
        try {
            LOG.debug("Start executing the task {}", new Object[]{task});
            super.execute(task);
        } catch (Exception e) {
            LOG.error("Error while executing the task {}", new Object[]{task}, e);
        } finally {
            semaphore.release();
        }
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable task) {
        try {
            LOG.debug("{} is executing {}", new Object[]{thread, task});
            startTime.set(System.nanoTime());
            if (task instanceof ExecutableEntry) {
                final ExecutableEntry entry = (ExecutableEntry) task;
                final EventActor actor = entry.getActor();
                if (actor != null && actor.getBehavior() != null) {
                    actor.getBehavior().calledBeforeExecution(entry.getEventInstance());
                }
            }
        } catch (Exception e) {
            LOG.debug("Error while handling the pre-execution of the task {} ", new Object[]{task}, e);
        }
    }

    @Override
    protected void afterExecute(Runnable task, Throwable t) {
        try {
            long endTime = System.nanoTime();
            long taskTime = endTime - startTime.get();
            numTasks.incrementAndGet();
            totalTime.addAndGet(taskTime);
            if (task instanceof ExecutableEntry) {
                final ExecutableEntry entry = (ExecutableEntry) task;
                final EventActor actor = entry.getActor();
                if (actor != null) {
                    if (actor.getBehavior() != null) {
                        actor.getBehavior().calledAfterExecution(entry.getEventInstance());
                    }
                    actor.fire(entry.getEventInstance());
                }
            }
        } catch (Exception e) {
            LOG.debug("Error while handling the post-execution of the task {} ", new Object[]{task}, e);
        }
    }

    @Override
    protected void terminated() {
        LOG.debug("Terminated: avg time={} (ms)", new Object[]{1.0 * totalTime.get() / numTasks.get() / TO_MILLISECOND});
    }
}
