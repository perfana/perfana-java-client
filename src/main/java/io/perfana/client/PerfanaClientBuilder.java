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
import io.perfana.event.factory.PerfanaEventScheduleDefaultFactory;
import io.perfana.event.factory.PerfanaEventScheduleFactory;
import io.perfana.event.factory.PerfanaEventScheduleFactoryProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfanaClientBuilder {

    private PerfanaTestContext perfanaTestContext;

    private PerfanaEventScheduleFactory eventScheduleFactory;
    private Map<String, String> eventScheduleFactoryProperties;

    private PerfanaConnectionSettings perfanaConnectionSettings;

    private boolean assertResultsEnabled = false;

    private PerfanaEventBroadcaster broadcaster;
    private PerfanaEventProperties eventProperties = new PerfanaEventProperties();

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

        List<ScheduleEvent> scheduleEvents = Collections.emptyList();
        if (eventScheduleFactory != null) {
            scheduleEvents = eventScheduleFactory.createPerfanaTestEvents(perfanaTestContext, eventScheduleFactoryProperties);
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
    public PerfanaClientBuilder setCustomPerfanaEvents(String perfanaCustomEventsText) {

        if (perfanaCustomEventsText == null) {
            this.eventScheduleFactory = new PerfanaEventScheduleDefaultFactory();
            this.eventScheduleFactoryProperties = Collections.emptyMap();
            return this;
        }

        if (perfanaCustomEventsText.contains("#factory-class")) {
            PerfanaEventScheduleFactoryProvider provider =
                    PerfanaEventScheduleFactoryProvider.createInstanceFromClasspath(logger);

            // TODO parse
            String factoryClassname = "nl.stokpop.perfana.event.StokpopEventScheduleFactory";

            PerfanaEventScheduleFactory factory =
                    provider.find(factoryClassname);

            if (factory == null) {
                throw new PerfanaClientRuntimeException("unable to find class: " + factoryClassname);
            }

            // TODO parse
            Map<String, String> properties = new HashMap<>();
            properties.put("slowbackend-file", "data/slowbackend.json");

            this.eventScheduleFactory = factory;
            this.eventScheduleFactoryProperties = properties;
        }
        else {
            Map<String, String> properties = new HashMap<>();
            properties.put("eventSchedule", perfanaCustomEventsText);

            this.eventScheduleFactory = new PerfanaEventScheduleDefaultFactory();
            this.eventScheduleFactoryProperties = properties;

        }
        return this;
    }

}