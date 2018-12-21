package io.perfana.service;

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
     */
    void beforeTest(String testId);

    /**
     * Called after the test run is done. Use for instance to start creating a report of some sort or
     * remove the test environment.
     * @param testId the test run id
     */
    void afterTest(String testId);

    /**
     * Called when a failover situation should be tested in the middle of the test run.
     * @param testId the test run id
     */
    void failover(String testId);

    /**
     * Called for each keep alive event for this test run.
     * @param testId the test run id
     */
    void keepAlive(String testId);

}
