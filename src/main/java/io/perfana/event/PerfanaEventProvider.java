package io.perfana.event;

import io.perfana.client.api.PerfanaClientLogger;
import io.perfana.client.api.PerfanaTestContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;

public class PerfanaEventProvider implements PerfanaEventBroadcaster {

    private final PerfanaClientLogger logger;

    private final List<PerfanaTestEvent> perfanaEvents;

    PerfanaEventProvider(List<PerfanaTestEvent> perfanaEvents, PerfanaClientLogger logger) {
        this.perfanaEvents = Collections.unmodifiableList(new ArrayList<>(perfanaEvents));
        this.logger = logger;
    }

    public static PerfanaEventProvider createInstanceWithEventsFromClasspath(PerfanaClientLogger logger) {
        ServiceLoader<PerfanaTestEvent> perfanaEventLoader = ServiceLoader.load(PerfanaTestEvent.class);
        // java 9+: List<PerfanaTestEvent> events = perfanaEventLoader.stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
        List<PerfanaTestEvent> events = new ArrayList<>();
        for (PerfanaTestEvent event : perfanaEventLoader) {
            events.add(event);
        }
        return new PerfanaEventProvider(events, logger);
    }

    @Override
    public void broadcastBeforeTest(PerfanaTestContext context, PerfanaEventProperties properties) {
        logger.info("broadcast before test event");
        perfanaEvents.forEach(catchExceptionWrapper(event -> event.beforeTest(context, properties.get(event))));
    }

    @Override
    public void broadcastAfterTest(PerfanaTestContext context, PerfanaEventProperties properties) {
        logger.info("broadcast after test event");
        perfanaEvents.forEach(catchExceptionWrapper(event -> event.afterTest(context, properties.get(event))));
    }
    
    @Override
    public void broadCastKeepAlive(PerfanaTestContext context, PerfanaEventProperties properties) {
        logger.debug("broadcast keep alive event");
        perfanaEvents.forEach(catchExceptionWrapper(event -> event.keepAlive(context, properties.get(event))));

    }

    @Override
    public void broadcastCustomEvent(PerfanaTestContext context, PerfanaEventProperties properties, ScheduleEvent scheduleEvent) {
        logger.info("broadcast " + scheduleEvent.getName() + " custom event");
        perfanaEvents.forEach(catchExceptionWrapper(event -> event.customEvent(context, properties.get(event), scheduleEvent)));
    }

    /**
     * Make sure events continue, even when exceptions are thrown.
     */
    private Consumer<PerfanaTestEvent> catchExceptionWrapper(Consumer<PerfanaTestEvent> consumer) {
        return event -> {
            try {
                consumer.accept(event);
            } catch (Exception e) {
                String message = String.format("exception in perfana event (%s)", event.getName());
                if (logger != null) {
                    logger.error(message, e);
                }
                else {
                    System.out.println("(note: better provide a logger): " + message);
                }
            }
        };
    }

}
