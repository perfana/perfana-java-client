package io.perfana.client;

import io.perfana.client.api.PerfanaCaller;
import io.perfana.client.api.PerfanaClientLogger;
import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.TestContext;
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

class PerfanaExecutorEngine {

    private final PerfanaClientLogger logger;

    private ScheduledExecutorService executorKeepAlive;
    private ScheduledExecutorService executorCustomEvents;

    PerfanaExecutorEngine(PerfanaClientLogger logger) {
        if (logger == null) {
            throw new PerfanaClientRuntimeException("logger is null");
        }
        this.logger = logger;
    }

    void startKeepAliveThread(PerfanaCaller perfana, TestContext context, PerfanaConnectionSettings settings, PerfanaEventBroadcaster broadcaster, PerfanaEventProperties eventProperties) {
        nullChecks(perfana, context, broadcaster, eventProperties);

        if (executorKeepAlive != null) {
            throw new RuntimeException("cannot start keep alive thread multiple times!");
        }

        logger.info(String.format("calling Perfana (%s) keep alive every %s", settings.getPerfanaUrl(), settings.getKeepAliveDuration()));

        executorKeepAlive = createKeepAliveScheduler();
        
        KeepAliveRunner keepAliveRunner = new KeepAliveRunner(perfana, context, broadcaster, eventProperties);
        executorKeepAlive.scheduleAtFixedRate(keepAliveRunner, 0, settings.getKeepAliveDuration().getSeconds(), TimeUnit.SECONDS);
    }

    private void nullChecks(PerfanaCaller perfana, TestContext context, PerfanaEventBroadcaster broadcaster, PerfanaEventProperties eventProperties) {
        if (perfana == null) {
            throw new NullPointerException("PerfanaCaller cannot be null");
        }
        if (context == null) {
            throw new NullPointerException("TestContext cannot be null");
        }
        if (broadcaster == null) {
            throw new NullPointerException("PerfanaEventBroadcaster cannot be null");
        }
        if (eventProperties == null) {
            throw new NullPointerException("PerfanaEventProperties cannot be null");
        }
    }

    private void addToExecutor(ScheduledExecutorService executorService, TestContext context, ScheduleEvent event, PerfanaEventProperties eventProperties, PerfanaCaller perfana, PerfanaEventBroadcaster broadcaster) {
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

    void startCustomEventScheduler(PerfanaCaller perfana, TestContext context, List<ScheduleEvent> scheduleEvents, PerfanaEventBroadcaster broadcaster, PerfanaEventProperties eventProperties) {
        nullChecks(perfana, context, broadcaster, eventProperties);

        if (!(scheduleEvents == null || scheduleEvents.isEmpty())) {

            logger.info(createEventScheduleMessage(scheduleEvents));

            executorCustomEvents = createCustomEventScheduler();
            scheduleEvents.forEach(event -> addToExecutor(executorCustomEvents, context, event, eventProperties, perfana, broadcaster));
        }
        else {
            logger.info("no custom Perfana schedule events found");
        }
    }

    public static String createEventScheduleMessage(List<ScheduleEvent> scheduleEvents) {
        StringBuilder message = new StringBuilder();
        message.append("=== custom Perfana events schedule ===");
        scheduleEvents.forEach(event -> message
                .append("\n==> ")
                .append(String.format("ScheduleEvent %-36.36s [fire-at=%-8s settings=%-50.50s]", event.getNameDescription(), event.getDuration(), event.getSettings())));
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
    
    class KeepAliveRunner implements Runnable {

        private final PerfanaCaller perfana;
        private final TestContext context;
        private final PerfanaEventBroadcaster broadcaster;
        private final PerfanaEventProperties eventProperties;

        KeepAliveRunner(PerfanaCaller perfana, TestContext context, PerfanaEventBroadcaster broadcaster, PerfanaEventProperties eventProperties) {
            this.perfana = perfana;
            this.context = context;
            this.broadcaster = broadcaster;
            this.eventProperties = eventProperties;
        }

        @Override
        public void run() {
            try {
                perfana.callPerfanaTestEndpoint(context, false);
            } catch (Exception e) {
                logger.error("Perfana call for keep-alive failed", e);
            }
            try {
                broadcaster.broadCastKeepAlive(context, eventProperties);
            } catch (Exception e) {
                logger.error("Perfana broadcast keep-alive failed", e);
            }

        }

        @Override
        public String toString() {
            return "KeepAliveRunner for " + context.getTestRunId();
        }
    }

    class EventRunner implements Runnable {

        private final ScheduleEvent event;

        private final TestContext context;
        private final PerfanaEventProperties eventProperties;

        private final PerfanaEventBroadcaster eventBroadcaster;
        private final PerfanaCaller perfana;

        public EventRunner(TestContext context, PerfanaEventProperties eventProperties, ScheduleEvent event, PerfanaEventBroadcaster eventBroadcaster, PerfanaCaller perfana) {
            this.event = event;
            this.context = context;
            this.eventProperties = eventProperties;
            this.eventBroadcaster = eventBroadcaster;
            this.perfana = perfana;
        }

        @Override
        public void run() {
            try {
                perfana.callPerfanaEvent(context, event.getDescription());
            } catch (Exception e) {
                logger.error("Perfana call event failed", e);
            }
            try {
                eventBroadcaster.broadcastCustomEvent(context, eventProperties, event);
            } catch (Exception e) {
                logger.error("Perfana broadcast event failed", e);
            }
        }

        @Override
        public String toString() {
            return String.format("EventRunner for event %s for testId %s", event, context.getTestRunId());
        }
    }

}
