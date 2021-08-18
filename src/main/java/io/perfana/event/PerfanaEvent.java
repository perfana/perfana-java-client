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
import nl.stokpop.eventscheduler.api.message.EventMessageBus;
import nl.stokpop.eventscheduler.api.message.EventMessageReceiver;
import nl.stokpop.eventscheduler.exception.handler.KillSwitchException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerfanaEvent extends EventAdapter<PerfanaEventContext> {

    private final String CLASSNAME = PerfanaEvent.class.getName();
    private final String eventName;

    private final TestContext perfanaTestContext;

    private final EventMessageBus messageBus;

    private final Map<String,String> receivedVariables = new ConcurrentHashMap<>();

    private PerfanaClient perfanaClient;
    private String abortDetailMessage = null;
    // save some state to do the status check
    private EventCheck eventCheck;

    PerfanaEvent(PerfanaEventContext context, EventMessageBus messageBus, EventLogger logger) {
        super(context, messageBus, logger);
        this.eventCheck = new EventCheck(context.getName(), CLASSNAME, EventStatus.UNKNOWN, "No known result yet. Try again some time later.");
        this.eventName = context.getName();
        this.perfanaTestContext = createPerfanaTestContext(context);
        this.messageBus = messageBus;

        EventMessageReceiver eventMessageReceiver = m -> {
            if (!m.getVariables().isEmpty()) {
                logger.info("received variables from " + m.getPluginName() + ": " + m.getVariables());
                receivedVariables.putAll(m.getVariables());
            }
        };
        this.messageBus.addReceiver(eventMessageReceiver);
    }

    @Override
    public void beforeTest() {

        PerfanaConnectionSettings settings = new PerfanaConnectionSettingsBuilder()
                .setPerfanaUrl(eventContext.getPerfanaUrl())
                .setApiKey("perfana-api-key-abc")
                .build();

        PerfanaClientBuilder builder = new PerfanaClientBuilder()
                .setLogger(new PerfanaClientEventLogger(logger))
                .setTestContext(perfanaTestContext)
                .setPerfanaConnectionSettings(settings)
                .setAssertResultsEnabled(eventContext.isAssertResultsEnabled());

        perfanaClient = builder.build();
    }

    @Override
    public void startTest() {
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
        perfanaClient.callPerfanaTestEndpoint(perfanaTestContext, true, receivedVariables);

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
            perfanaClient.callPerfanaTestEndpoint(perfanaTestContext, false, receivedVariables);
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

    private static TestContext createPerfanaTestContext(PerfanaEventContext context) {

        nl.stokpop.eventscheduler.api.config.TestContext testContext = context.getTestContext();

        if (testContext == null) {
            throw new PerfanaClientRuntimeException("testConfig in eventConfig is null: " + context);
        }

        return new TestContextBuilder()
            .setVariables(context.getVariables())
            .setTags(testContext.getTags())
            .setAnnotations(testContext.getAnnotations())
            .setSystemUnderTest(testContext.getSystemUnderTest())
            .setVersion(testContext.getVersion())
            .setCIBuildResultsUrl(testContext.getBuildResultsUrl())
            .setConstantLoadTime(testContext.getConstantLoadTime())
            .setRampupTime(testContext.getRampupTime())
            .setTestEnvironment(testContext.getTestEnvironment())
            .setTestRunId(testContext.getTestRunId())
            .setWorkload(testContext.getWorkload())
            .build();
    }

}
