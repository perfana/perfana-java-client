package io.perfana.event;

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
     * Called before the test run starts. You can for instance cleanup the test environment and/or
     * restart the server under test.
     * @param testId the test run id
     * @param eventProperties properties for this event, e.g. REST_URL="https://my-rest-url"
     */
    void beforeTest(String testId, Map<String,String> eventProperties);

    /**
     * Called after the test run is done. Use for instance to start creating a report of some sort or
     * remove the test environment.
     * @param testId the test run id
     * @param eventProperties properties for this event, e.g. REST_URL="https://my-rest-url"
     */
    void afterTest(String testId, Map<String,String> eventProperties);

    /**
     * Called when a failover situation should be tested in the middle of the test run.
     * @param testId the test run id
     * @param eventProperties properties for this event, e.g. REST_URL="https://my-rest-url"
     */
    void failover(String testId, Map<String,String> eventProperties);

    /**
     * Called for each keep alive event for this test run.
     * @param testId the test run id
     * @param eventProperties properties for this event, e.g. REST_URL="https://my-rest-url"
     */
    void keepAlive(String testId, Map<String,String> eventProperties);

}
