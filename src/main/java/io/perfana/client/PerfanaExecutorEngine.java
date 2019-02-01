package io.perfana.client;

import io.perfana.client.api.PerfanaCaller;
import io.perfana.client.api.PerfanaClientLogger;
import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.PerfanaTestContext;
import io.perfana.client.exception.PerfanaClientRuntimeException;
import io.perfana.event.PerfanaEventBroadcaster;
import io.perfana.event.PerfanaEventProperties;
import io.perfana.event.ScheduleEvent;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PerfanaExecutorEngine {

    private PerfanaClientLogger logger;

    private ScheduledExecutorService executorKeepAlive;
    private ScheduledExecutorService executorCustomEvents;

    public PerfanaExecutorEngine(PerfanaClientLogger logger) {
        if (logger == null) {
            throw new PerfanaClientRuntimeException("logger is null");
        }
        this.logger = logger;
    }

    public void startKeepAliveThread(PerfanaCaller perfana, PerfanaTestContext context, PerfanaConnectionSettings settings, PerfanaEventBroadcaster broadcaster, PerfanaEventProperties eventProperties) {
        if (executorKeepAlive != null) {
            throw new RuntimeException("cannot start keep alive thread multiple times!");
        }

        logger.info(String.format("calling Perfana (%s) keep alive every %s", settings.getPerfanaUrl(), settings.getKeepAliveDuration()));

        executorKeepAlive = createKeepAliveScheduler();
        
        KeepAliveRunner keepAliveRunner = new KeepAliveRunner(perfana, context, broadcaster, eventProperties);
        executorKeepAlive.scheduleAtFixedRate(keepAliveRunner, 0, settings.getKeepAliveDuration().getSeconds(), TimeUnit.SECONDS);
    }

    private void addToExecutor(ScheduledExecutorService executorService, PerfanaTestContext context, ScheduleEvent event, PerfanaEventProperties eventProperties, PerfanaCaller perfana, PerfanaEventBroadcaster broadcaster) {
        executorService.schedule(new EventRunner(context, eventProperties, event, broadcaster, perfana), event.getDuration().getSeconds(), TimeUnit.SECONDS);
    }

    void shutdownThreadsNow() {
        logger.info("shutdown Perfana Executor threads");
        if (executorKeepAlive != null) {
            executorKeepAlive.shutdownNow();
        }
        if (executorCustomEvents != null) {
            List<Runnable> runnables = executorCustomEvents.shutdownNow();
            if (runnables.size() > 0) {
                if (runnables.size() == 1) {
                    logger.warn("there is 1 custom Perfana event that is not (fully) executed!");
                }
                else {
                    logger.warn("there are " + runnables.size() + " custom Perfana events that are not (fully) executed!");
                }
            }
        }
        executorKeepAlive = null;
        executorCustomEvents = null;
    }

    public void startCustomEventScheduler(PerfanaCaller perfana, PerfanaTestContext context, List<ScheduleEvent> scheduleEvents, PerfanaEventBroadcaster broadcaster, PerfanaEventProperties eventProperties) {
        if (!(scheduleEvents == null || scheduleEvents.isEmpty())) {

            logger.info(createEventScheduleMessage(scheduleEvents));

            executorCustomEvents = createCustomEventScheduler();
            scheduleEvents.forEach(event -> addToExecutor(executorCustomEvents, context, event, eventProperties, perfana, broadcaster));
        }
        else {
            logger.info("no custom Perfana schedule events found");
        }
    }

    private String createEventScheduleMessage(List<ScheduleEvent> scheduleEvents) {
        StringBuilder message = new StringBuilder();
        message.append("=== custom Perfana events schedule ===");
        scheduleEvents.forEach(event -> message
                .append("\n==> ")
                .append(String.format("ScheduleEvent %-16s [fire-at=%-8s settings=%s]", event.getName(), event.getDuration(), event.getSettings())));
        return message.toString();
    }

    private ScheduledExecutorService createKeepAliveScheduler() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            String threadName = "Perfana-Keep-Alive-Thread";
            logger.info("create new thread: " + threadName);
            return new Thread(r, threadName);
        });
    }

    private ScheduledExecutorService createCustomEventScheduler() {
        return Executors.newScheduledThreadPool(2, new ThreadFactory() {
            private final AtomicInteger perfanaThreadCount = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                String threadName = "Perfana-Custom-Event-Thread-" + perfanaThreadCount.incrementAndGet();
                logger.info("create new thread: " + threadName);
                return new Thread(r, threadName);
            }
        });
    }
    
    public static class KeepAliveRunner implements Runnable {

        private final PerfanaCaller perfana;
        private final PerfanaTestContext context;
        private final PerfanaEventBroadcaster broadcaster;
        private final PerfanaEventProperties eventProperties;

        KeepAliveRunner(PerfanaCaller perfana, PerfanaTestContext context, PerfanaEventBroadcaster broadcaster, PerfanaEventProperties eventProperties) {
            this.perfana = perfana;
            this.context = context;
            this.broadcaster = broadcaster;
            this.eventProperties = eventProperties;
        }

        @Override
        public void run() {
            perfana.callPerfanaTestEndpoint(context, false);
            broadcaster.broadCastKeepAlive(context, eventProperties);
        }

        @Override
        public String toString() {
            return "KeepAliveRunner for " + context.getTestRunId();
        }
    }

    public static class EventRunner implements Runnable {

        private final ScheduleEvent event;

        private final PerfanaTestContext context;
        private final PerfanaEventProperties eventProperties;

        private final PerfanaEventBroadcaster eventBroadcaster;
        private final PerfanaCaller perfana;

        public EventRunner(PerfanaTestContext context, PerfanaEventProperties eventProperties, ScheduleEvent event, PerfanaEventBroadcaster eventBroadcaster, PerfanaCaller perfana) {
            this.event = event;
            this.context = context;
            this.eventProperties = eventProperties;
            this.eventBroadcaster = eventBroadcaster;
            this.perfana = perfana;
        }

        @Override
        public void run() {
            perfana.callPerfanaEvent(context, event.getName());
            eventBroadcaster.broadcastCustomEvent(context, eventProperties, event);
        }

        @Override
        public String toString() {
            return String.format("EventRunner for event %s for testId %s", event, context.getTestRunId());
        }
    }

}
