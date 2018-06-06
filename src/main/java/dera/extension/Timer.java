package dera.extension;

import dera.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Timer extends Trigger {

    private static final Logger LOG = LoggerFactory.getLogger(Timer.class);
    private final long interval;
    private final ScheduledExecutorService executor;
    private final TimeUnit timeUnit;
    private final long initialDelay;

    public Timer(String id, long initialDelay, long interval, TimeUnit timeUnit) {
        super(id);
        this.interval = interval;
        this.timeUnit = timeUnit;
        this.initialDelay = initialDelay;
        this.executor = Executors.newScheduledThreadPool(1);
    }

    @Override
    public final void start(String correlationId) {
        executor.scheduleAtFixedRate(new TimedTriggerTask(), initialDelay, interval, timeUnit);
    }

    public final void stop() {
        try {
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    class TimedTriggerTask implements Runnable {
        @Override
        public void run() {
            String correlationId = TextUtil.randomFixedLengthNumber();
            LOG.info("{} with the correlation ID {} are gonna be fired", new Object[]{getOutput(), correlationId});
            try {
                publish(correlationId, Collections.EMPTY_MAP, output);
            } catch (Exception ignored) {
            }
        }
    }
}
