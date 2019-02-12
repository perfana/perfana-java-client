package io.perfana.client;

import io.perfana.client.api.PerfanaClientLogger;
import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.PerfanaTestContext;
import io.perfana.client.exception.PerfanaClientRuntimeException;
import io.perfana.event.EventScheduleGenerator;
import io.perfana.event.PerfanaEventBroadcaster;
import io.perfana.event.PerfanaEventProperties;
import io.perfana.event.PerfanaEventProvider;
import io.perfana.event.ScheduleEvent;
import io.perfana.event.generator.EventScheduleGeneratorDefault;
import io.perfana.event.generator.EventScheduleGeneratorProvider;
import io.perfana.event.generator.GeneratorProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfanaClientBuilder {

    private static final String GENERATOR_CLASS_META_TAG = "@generator-class";

    private PerfanaTestContext perfanaTestContext;
    
    private PerfanaConnectionSettings perfanaConnectionSettings;

    private boolean assertResultsEnabled = false;

    private PerfanaEventBroadcaster broadcaster;
    private PerfanaEventProperties eventProperties = new PerfanaEventProperties();

    private String customEventsText = "";

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
        if (broadcaster == null) {
            PerfanaClientLogger logger = perfanaTestContext.getLogger();
            logger.info("create default Perfana event broadcaster");
            broadcaster = PerfanaEventProvider.createInstanceWithEventsFromClasspath(logger);
        }
        
        if (perfanaTestContext == null) {
            throw new PerfanaClientRuntimeException("PerfanaTestContext must be set, it is null.");
        }

        if (perfanaConnectionSettings == null) {
            throw new PerfanaClientRuntimeException("PerfanaConnectionSettings must be set, it is null.");
        }

        List<ScheduleEvent> scheduleEvents = generateEventSchedule(perfanaTestContext, customEventsText);

        return new PerfanaClient(perfanaTestContext, perfanaConnectionSettings, assertResultsEnabled,
                broadcaster, eventProperties, scheduleEvents);
    }

    private static List<ScheduleEvent> generateEventSchedule(PerfanaTestContext context, String text) {
        EventScheduleGenerator eventScheduleGenerator;
        GeneratorProperties generatorProperties;

        if (text == null) {
            eventScheduleGenerator = new EventScheduleGeneratorDefault();
            generatorProperties = new GeneratorProperties();
        }
        else if (text.contains(GENERATOR_CLASS_META_TAG)) {

            generatorProperties = new GeneratorProperties(text);

            String generatorClassname = generatorProperties.getMetaProperty(GENERATOR_CLASS_META_TAG);

            eventScheduleGenerator = findAndCreateEventScheduleGenerator(context.getLogger(), generatorClassname);
        }
        else {
            // assume the default input of lines of events
            Map<String, String> properties = new HashMap<>();
            properties.put("eventSchedule", text);

            eventScheduleGenerator = new EventScheduleGeneratorDefault();
            generatorProperties = new GeneratorProperties(properties);
        }

        List<ScheduleEvent> scheduleEvents = Collections.emptyList();
        if (eventScheduleGenerator != null) {
            scheduleEvents = eventScheduleGenerator.generateEvents(context, generatorProperties);
        }
        return scheduleEvents;
    }

    /**
     * Provide schedule event as "duration|eventname(description)|json-settings".
     * The duration is in ISO-8601 format period format, e.g. 3 minutes 15 seconds
     * is P3M15S.
     *
     * One schedule event per line.
     *
     * Or provide an EventScheduleGenerator implementation as:
     *
     * <pre>
     *      {@literal @}generator-class=nl.stokpop.event.MyEventGenerator
     *      foo=bar
     * </pre>
     */
    public PerfanaClientBuilder setCustomEvents(String customEventsText) {
        if (customEventsText != null) {
            this.customEventsText = customEventsText;
        }
        return this;
    }

    private static EventScheduleGenerator findAndCreateEventScheduleGenerator(PerfanaClientLogger logger, String generatorClassname) {
        EventScheduleGeneratorProvider provider =
                EventScheduleGeneratorProvider.createInstanceFromClasspath(logger);

        EventScheduleGenerator generator = provider.find(generatorClassname);

        if (generator == null) {
            throw new PerfanaClientRuntimeException("unable to find EventScheduleGenerator implementation class: " + generatorClassname);
        }
        return generator;
    }
    
}