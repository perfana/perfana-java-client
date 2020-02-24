/**
 * Perfana Java Client - Java library that talks to the Perfana server
 * Copyright (C) 2020  Peter Paul Bakker @ Stokpop, Daniel Moll @ Perfana.io
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.perfana.event;

import io.perfana.client.api.PerfanaClientLogger;
import io.perfana.client.api.TestContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;

public class PerfanaEventProvider implements PerfanaEventBroadcaster {

    private final PerfanaClientLogger logger;

    private final List<PerfanaEvent> perfanaEvents;

    PerfanaEventProvider(List<PerfanaEvent> perfanaEvents, PerfanaClientLogger logger) {
        this.perfanaEvents = Collections.unmodifiableList(new ArrayList<>(perfanaEvents));
        this.logger = logger;
    }

    public static PerfanaEventProvider createInstanceWithEventsFromClasspath(PerfanaClientLogger logger) {
        return createInstanceWithEventsFromClasspath(logger, null);
    }

    public static PerfanaEventProvider createInstanceWithEventsFromClasspath(PerfanaClientLogger logger, ClassLoader classLoader) {
        ServiceLoader<PerfanaEvent> perfanaEventLoader = classLoader == null
                ? ServiceLoader.load(PerfanaEvent.class)
                : ServiceLoader.load(PerfanaEvent.class, classLoader);
        // java 9+: List<PerfanaEvent> events = perfanaEventLoader.stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
        List<PerfanaEvent> events = new ArrayList<>();
        for (PerfanaEvent event : perfanaEventLoader) {
            events.add(event);
        }
        return new PerfanaEventProvider(events, logger);
    }

    @Override
    public void broadcastBeforeTest(TestContext context, PerfanaEventProperties properties) {
        logger.info("broadcast before test event");
        perfanaEvents.forEach(catchExceptionWrapper(event -> event.beforeTest(context, properties.get(event))));
    }

    @Override
    public void broadcastAfterTest(TestContext context, PerfanaEventProperties properties) {
        logger.info("broadcast after test event");
        perfanaEvents.forEach(catchExceptionWrapper(event -> event.afterTest(context, properties.get(event))));
    }
    
    @Override
    public void broadCastKeepAlive(TestContext context, PerfanaEventProperties properties) {
        logger.debug("broadcast keep alive event");
        perfanaEvents.forEach(catchExceptionWrapper(event -> event.keepAlive(context, properties.get(event))));

    }

    @Override
    public void broadcastCustomEvent(TestContext context, PerfanaEventProperties properties, ScheduleEvent scheduleEvent) {
        logger.info("broadcast " + scheduleEvent.getName() + " custom event");
        perfanaEvents.forEach(catchExceptionWrapper(event -> event.customEvent(context, properties.get(event), scheduleEvent)));
    }

    /**
     * Make sure events continue, even when exceptions are thrown.
     */
    private Consumer<PerfanaEvent> catchExceptionWrapper(Consumer<PerfanaEvent> consumer) {
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
