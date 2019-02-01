package io.perfana.event;

import io.perfana.client.api.PerfanaTestContext;

import java.util.Map;

/**
 * This interface can be implemented in other jars and be put on the classpath.
 *
 * Provide a file in META-INF/services/io.perfana.service.PerfanaTestEvent that contains the
 * fully qualified name of the implementing class.
 *
 * This class will be used when these events are called. Possibly you can even provide multiple implementations
 * on the classpath that will all be called.
 *
 * For more information on how this technically works, check out javadoc of java.util.ServiceLoader.
 */
public interface PerfanaTestEvent {

    /**
     * @return name of the test event.
     */
    String name();

    /**
     * Called before the test run starts. You can for instance cleanup the test environment and/or
     * restart the server under test.
     * @param context the test run context
     * @param eventProperties properties for this event, e.g. REST_URL="https://my-rest-url"
     */
    void beforeTest(PerfanaTestContext context, Map<String,String> eventProperties);

    /**
     * Called after the test run is done. Use for instance to start creating a report of some sort or
     * remove the test environment.
     * @param context the test run context
     * @param eventProperties properties for this event, e.g. REST_URL="https://my-rest-url"
     */
    void afterTest(PerfanaTestContext context, Map<String,String> eventProperties);
    
    /**
     * Called for each keep alive event for this test run.
     * @param context the test run context
     * @param eventProperties properties for this event, e.g. REST_URL="https://my-rest-url"
     */
    void keepAlive(PerfanaTestContext context, Map<String,String> eventProperties);

    /**
     * Called for each custom event, according to the custom even schedule.
     * @param context the test run context
     * @param eventProperties properties for this event, e.g. REST_URL="https://my-rest-url"
     * @param scheduleEvent the custom event, use to execute specific behaviour in the event handler
     */
    void customEvent(PerfanaTestContext context, Map<String,String> eventProperties, ScheduleEvent scheduleEvent);
    
}
