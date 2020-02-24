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
package io.perfana.event2;

import io.perfana.client.PerfanaClient;
import io.perfana.client.PerfanaClientBuilder;
import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.PerfanaConnectionSettingsBuilder;
import io.perfana.client.api.TestContextBuilder;
import io.perfana.client.exception.PerfanaAssertionsAreFalse;
import io.perfana.client.exception.PerfanaClientException;
import nl.stokpop.eventscheduler.api.*;
import nl.stokpop.eventscheduler.exception.KillSwitchException;

import java.util.Collection;
import java.util.Set;

public class PerfanaEvent2 extends EventAdapter {

    private final String CLASSNAME = PerfanaEvent2.class.getName();

    private static final Set<String> ALLOWED_PROPERTIES = setOf("perfanaUrl");

    private PerfanaClient perfanaClient;
    private io.perfana.client.api.TestContext perfanaTestContext;

    private String abortDetailMessage = null;

    // save some state to do the status check
    private EventCheck eventCheck;

    PerfanaEvent2(String name, TestContext context, EventProperties properties, EventLogger logger) {
        super(name, context, properties, logger);
        this.eventCheck = new EventCheck(eventName, CLASSNAME, EventStatus.UNKNOWN, "No known result yet. Try again some time later.");
        this.perfanaTestContext = createPerfanaTestContext(context);
    }

    @Override
    public Collection<String> allowedProperties() {
        return ALLOWED_PROPERTIES;
    }

    @Override
    public void beforeTest() {

        PerfanaConnectionSettings settings = new PerfanaConnectionSettingsBuilder()
                .setPerfanaUrl(eventProperties.getPropertyOrDefault("perfanaUrl", "http://localhost:8888"))
                .build();

        PerfanaClientBuilder builder = new PerfanaClientBuilder()
                .setLogger(new EventPerfanaClientLogger(logger))
                .setTestContext(perfanaTestContext)
                .setPerfanaConnectionSettings(settings)
                .setAssertResultsEnabled(Boolean.parseBoolean(eventProperties.getPropertyOrDefault("assertResultsEnabled", "true")));
                // this is not needed, events are already in event-scheduler! .setCustomEvents(eventProperties.getPropertyOrDefault("eventScheduleScript", ""));

        perfanaClient = builder.build();

        perfanaClient.callPerfanaEvent(perfanaTestContext, "Test start");
    }

    @Override
    public void afterTest() {
        perfanaClient.callPerfanaEvent(perfanaTestContext, "Test finish");
        perfanaClient.callPerfanaTestEndpoint(perfanaTestContext, true);

        try {
            String text = perfanaClient.assertResults();
            logger.info(String.format("the assertion text: %s", text));
        } catch (PerfanaClientException e) {
            logger.error("Stop Perfana session call failed.", e);
        } catch (PerfanaAssertionsAreFalse perfanaAssertionsAreFalse) {
            eventCheck = new EventCheck(eventName, CLASSNAME, EventStatus.FAILURE, perfanaAssertionsAreFalse.getMessage());
        }
        eventCheck = new EventCheck(eventName, CLASSNAME, EventStatus.SUCCESS, "All ok!");
    }

    @Override
    public void abortTest() {
        String eventDescription = "Test aborted" + (abortDetailMessage == null ? "" : ": " + abortDetailMessage);
        perfanaClient.callPerfanaEvent(perfanaTestContext, eventDescription);
        this.eventCheck = new EventCheck(eventName, CLASSNAME, EventStatus.ABORTED, eventDescription);
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
            perfanaClient.callPerfanaEvent(perfanaTestContext, customEvent.getDescription());
        } catch (Exception e) {
            logger.error("Perfana call event failed", e);
        }
    }

    private static io.perfana.client.api.TestContext createPerfanaTestContext(TestContext testContext) {
        return new TestContextBuilder()
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
    }

}
