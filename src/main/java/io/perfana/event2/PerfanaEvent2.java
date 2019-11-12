package io.perfana.event2;

import io.perfana.client.PerfanaClient;
import io.perfana.client.PerfanaClientBuilder;
import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.PerfanaConnectionSettingsBuilder;
import io.perfana.client.api.TestContextBuilder;
import io.perfana.client.exception.PerfanaAssertionsAreFalse;
import io.perfana.client.exception.PerfanaClientException;
import nl.stokpop.eventscheduler.api.*;

public class PerfanaEvent2 extends EventAdapter {

    private final String CLASSNAME = PerfanaEvent2.class.getName();

    private PerfanaClient perfanaClient;

    // save some state to do the status check
    private EventCheck eventCheck;

    PerfanaEvent2(String name, TestContext context, EventProperties properties, EventLogger logger) {
        super(name, context, properties, logger);
        this.eventCheck = new EventCheck(eventName, CLASSNAME, EventStatus.UNKNOWN, "No known result yet. Try again some time later.");
    }

    @Override
    public void beforeTest() {

        PerfanaConnectionSettings settings = new PerfanaConnectionSettingsBuilder()
                .setPerfanaUrl(eventProperties.getPropertyOrDefault("perfanaUrl", "http://localhost:8888"))
                .build();

        io.perfana.client.api.TestContext perfanaTestContext = new TestContextBuilder()
                .setVariables(testContext.getVariables())
                .setTags(testContext.getTags())
                .setAnnotations(testContext.getAnnotations())
                .setApplication(testContext.getApplication())
                .setApplicationRelease(testContext.getApplicationRelease())
                .setCIBuildResultsUrl(testContext.getCIBuildResultsUrl())
                .setConstantLoadTime(testContext.getPlannedDuration())
                .setRampupTime(testContext.getRampupTime())
                .setTestEnvironment(testContext.getTestEnvironment())
                .setTestRunId(testContext.getTestRunId())
                .setTestType(testContext.getTestType()).build();

        PerfanaClientBuilder builder = new PerfanaClientBuilder()
                .setLogger(new EventPerfanaClientLogger(logger))
                .setTestContext(perfanaTestContext)
                .setPerfanaConnectionSettings(settings)
                .setAssertResultsEnabled(Boolean.valueOf(eventProperties.getPropertyOrDefault("assertResultsEnabled", "true")));
                // this is not needed, events are already in event-scheduler! .setCustomEvents(eventProperties.getPropertyOrDefault("eventScheduleScript", ""));

        perfanaClient = builder.build();

        perfanaClient.startSession();
    }

    @Override
    public void afterTest() {
        try {
            perfanaClient.stopSession();
        } catch (PerfanaClientException e) {
            logger.error("Stop Perfana session call failed.", e);
        } catch (PerfanaAssertionsAreFalse perfanaAssertionsAreFalse) {
            eventCheck = new EventCheck(eventName, CLASSNAME, EventStatus.FAILURE, perfanaAssertionsAreFalse.getMessage());
        }
        eventCheck = new EventCheck(eventName, CLASSNAME, EventStatus.SUCCESS, "All ok!");
    }

    @Override
    public void abortTest() {
        perfanaClient.abortSession();
        this.eventCheck = new EventCheck(eventName, CLASSNAME, EventStatus.ABORTED, "Test run is aborted.");
    }

    @Override
    public EventCheck check() {
        return eventCheck;
    }

    @Override
    public void keepAlive() {
        logger.info("Keep alive called, no implementation in PerfanaEvent2 as of yet.");
    }
}
