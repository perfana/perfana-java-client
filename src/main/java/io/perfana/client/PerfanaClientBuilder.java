package io.perfana.client;

import io.perfana.client.api.PerfanaClientLogger;
import io.perfana.client.api.PerfanaClientLoggerStdOut;
import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.PerfanaTestContext;
import io.perfana.client.exception.PerfanaClientRuntimeException;
import io.perfana.event.PerfanaEventBroadcaster;
import io.perfana.event.PerfanaEventProperties;
import io.perfana.event.PerfanaEventProvider;
import io.perfana.event.ScheduleEvent;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PerfanaClientBuilder {

    private PerfanaTestContext perfanaTestContext;

    private PerfanaConnectionSettings perfanaConnectionSettings;

    private boolean assertResultsEnabled = false;

    private PerfanaEventBroadcaster broadcaster;
    private PerfanaEventProperties eventProperties = new PerfanaEventProperties();
    private List<ScheduleEvent> scheduleEvents = Collections.emptyList();

    private PerfanaClientLogger logger = new PerfanaClientLoggerStdOut();

    public PerfanaClientBuilder setPerfanaTestContext(PerfanaTestContext context) {
        this.perfanaTestContext = context;
        return this;
    }

    public PerfanaClientBuilder setPerfanaConnectionSettings(PerfanaConnectionSettings settings) {
        this.perfanaConnectionSettings = settings;
        return this;
    }

    public PerfanaClientBuilder setAssertResultsEnabled(boolean assertResultsEnabled) {
        this.assertResultsEnabled = assertResultsEnabled;
        return this;
    }

    public PerfanaClientBuilder setLogger(PerfanaClientLogger logger) {
        if (logger != null) {
            this.logger = logger;
        }
        return this;
    }

    public PerfanaClientBuilder setBroadcaster(PerfanaEventBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
        return this;
    }
    
    /**
     * Add properties to be passed on to the event implementation class.
     * @param eventImplementationName the fully qualified implementation class name (class.getName())
     * @param name the name of the property (not null or empty), e.g. "REST_URL"
     * @param value the name of the property (can be null or empty), e.g. "https://my-rest-call"
     * @return this
     */
    public PerfanaClientBuilder addEventProperty(String eventImplementationName, String name, String value) {
        if (eventImplementationName == null || eventImplementationName.isEmpty()) {
            throw new PerfanaClientRuntimeException("EventImplementationName is null or empty for " + this);
        }
        if (name == null || name.isEmpty()) {
            throw new PerfanaClientRuntimeException("EventImplementation property name is null or empty for " + this);
        }
        eventProperties.put(eventImplementationName, name, value);
        return this;
    }
    
    public PerfanaClient build() {

        // get default broadcaster if no broadcaster was given
        if (broadcaster == null) logger.info("create default Perfana event broadcaster");
        PerfanaEventBroadcaster broadcaster = this.broadcaster == null ?
                PerfanaEventProvider.createInstanceWithEventsFromClasspath(logger) : this.broadcaster;

        if (perfanaTestContext == null) {
            throw new PerfanaClientRuntimeException("PerfanaTestContext must be set, it is null.");
        }

        if (perfanaConnectionSettings == null) {
            throw new PerfanaClientRuntimeException("PerfanaConnectionSettings must be set, it is null.");
        }

        return new PerfanaClient(perfanaTestContext, perfanaConnectionSettings, assertResultsEnabled,
                broadcaster, eventProperties, scheduleEvents, logger);
    }
    
    /**
     * Provide schedule event as "duration|eventname(description)|json-settings".
     * The duration is in ISO-8601 format period format, e.g. 3 minutes 15 seconds
     * is P3M15S.
     *
     * One schedule event per line.
     */
    public PerfanaClientBuilder setScheduleEvents(String eventSchedule) {
        if (eventSchedule != null) {
            BufferedReader eventReader = new BufferedReader(new StringReader(eventSchedule));
            setScheduleEvents(eventReader.lines()
                    .map(String::trim)
                    .filter(e -> !e.isEmpty())
                    .collect(Collectors.toList()));
        }
        return this;
    }

    /**
     * Provide list of schedule events.
     * @see PerfanaClientBuilder#setScheduleEvents(String) 
     */
    public PerfanaClientBuilder setScheduleEvents(List<String> scheduleEvents) {
        if (scheduleEvents != null) {
            this.scheduleEvents = parseScheduleEvents(scheduleEvents);
        }
        return this;
    }

    private List<ScheduleEvent> parseScheduleEvents(List<String> eventSchedule) {
        return eventSchedule.stream()
                .map(ScheduleEvent::createFromLine)
                .collect(Collectors.toList());
    }
}