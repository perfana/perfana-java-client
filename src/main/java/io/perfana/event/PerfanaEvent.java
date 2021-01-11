/*
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

import io.perfana.client.PerfanaClient;
import io.perfana.client.PerfanaClientBuilder;
import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.PerfanaConnectionSettingsBuilder;
import io.perfana.client.api.TestContext;
import io.perfana.client.api.TestContextBuilder;
import io.perfana.client.exception.PerfanaAssertionsAreFalse;
import io.perfana.client.exception.PerfanaClientException;
import io.perfana.client.exception.PerfanaClientRuntimeException;
import nl.stokpop.eventscheduler.api.*;
import nl.stokpop.eventscheduler.api.config.TestConfig;
import nl.stokpop.eventscheduler.exception.handler.KillSwitchException;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

public class PerfanaEvent extends EventAdapter<PerfanaEventConfig> {

    private final String CLASSNAME = PerfanaEvent.class.getName();
    private final TestContext perfanaTestContext;
    private final String eventName;

    private PerfanaClient perfanaClient;
    private String abortDetailMessage = null;
    // save some state to do the status check
    private EventCheck eventCheck;

    PerfanaEvent(PerfanaEventConfig eventConfig, EventLogger logger) {
        super(eventConfig, logger);
        this.eventCheck = new EventCheck(eventConfig.getName(), CLASSNAME, EventStatus.UNKNOWN, "No known result yet. Try again some time later.");
        this.eventName = eventConfig.getName();
        this.perfanaTestContext = createPerfanaTestContext(eventConfig);
    }

    @Override
    public void beforeTest() {

        PerfanaConnectionSettings settings = new PerfanaConnectionSettingsBuilder()
                .setPerfanaUrl(eventConfig.getPerfanaUrl())
                .build();

        PerfanaClientBuilder builder = new PerfanaClientBuilder()
                .setLogger(new PerfanaClientEventLogger(logger))
                .setTestContext(createPerfanaTestContext(eventConfig))
                .setPerfanaConnectionSettings(settings)
                .setAssertResultsEnabled(eventConfig.isAssertResultsEnabled());

        perfanaClient = builder.build();

        perfanaClient.callPerfanaEvent(perfanaTestContext, "Test start", "Test run started");
    }

    @Override
    public void afterTest() {

        if (abortDetailMessage != null) {
            perfanaClient.callPerfanaEvent(perfanaTestContext, "Test abort", abortDetailMessage);
        }
        else {
            perfanaClient.callPerfanaEvent(perfanaTestContext, "Test end", "Test run completed");
        }

        finalizePerfanaTestRun();
    }

    /**
     * Calls out to Perfana with completed = true. Also checks the assertions of the test run.
     */
    private void finalizePerfanaTestRun() {
        perfanaClient.callPerfanaTestEndpoint(perfanaTestContext, true);

        // assume all is ok, will be overridden in case of assertResult exceptions
        eventCheck = new EventCheck(eventName, CLASSNAME, EventStatus.SUCCESS, "All ok!");
        try {
            String text = perfanaClient.assertResults();
            logger.info("Received Perfana check results: " + text);
        } catch (PerfanaClientException e) {
            logger.error("Perfana checks failed.", e);
            eventCheck = new EventCheck(eventName, CLASSNAME, EventStatus.FAILURE, "Failed to get check results: " + e.getMessage());
        } catch (PerfanaAssertionsAreFalse perfanaAssertionsAreFalse) {
            eventCheck = new EventCheck(eventName, CLASSNAME, EventStatus.FAILURE, perfanaAssertionsAreFalse.getMessage());
        }
    }

    @Override
    public void abortTest() {
        String eventTitle = "Test aborted";
        String eventDescription = abortDetailMessage == null ? "manually aborted" : abortDetailMessage;
        perfanaClient.callPerfanaEvent(perfanaTestContext, eventTitle, eventDescription);

        this.eventCheck = new EventCheck(eventName, CLASSNAME, EventStatus.ABORTED, eventDescription);

        // maybe only when not manually aborted? e.g. abortDetailMessage is set?
        finalizePerfanaTestRun();
    }

    @Override
    public EventCheck check() {
        return eventCheck;
    }

    @Override
    public void keepAlive() {
        logger.debug("Keep alive called");
        try {
            perfanaClient.callPerfanaTestEndpoint(perfanaTestContext, false);
        } catch (KillSwitchException killSwitchException) {
            abortDetailMessage = killSwitchException.getMessage();
            throw killSwitchException;
        }
    }

    @Override
    public void customEvent(CustomEvent customEvent) {
        try {
            perfanaClient.callPerfanaEvent(perfanaTestContext, customEvent.getName(), customEvent.getDescription());
        } catch (Exception e) {
            logger.error("Perfana call event failed", e);
        }
    }

    private static TestContext createPerfanaTestContext(PerfanaEventConfig eventConfig) {

        TestConfig testConfig = eventConfig.getTestConfig();

        if (testConfig == null) {
            throw new PerfanaClientRuntimeException("testConfig in eventConfig is null: " + eventConfig);
        }

        return new TestContextBuilder()
            .setVariables(eventConfig.getVariables())
            .setTags(testConfig.getTags())
            .setAnnotations(testConfig.getAnnotations())
            .setSystemUnderTest(testConfig.getSystemUnderTest())
            .setVersion(testConfig.getVersion())
            .setCIBuildResultsUrl(testConfig.getBuildResultsUrl())
            .setConstantLoadTime(Duration.of(testConfig.getConstantLoadTimeInSeconds(), SECONDS))
            .setRampupTime(Duration.of(testConfig.getRampupTimeInSeconds(), SECONDS))
            .setTestEnvironment(testConfig.getTestEnvironment())
            .setTestRunId(testConfig.getTestRunId())
            .setWorkload(testConfig.getWorkload())
            .build();
    }

}
