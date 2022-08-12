/*
 *    Copyright 2020-2022  Peter Paul Bakker @ Perfana.io, Daniel Moll @ Perfana.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.perfana.event;

import io.perfana.client.PerfanaClient;
import io.perfana.client.PerfanaClientBuilder;
import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.PerfanaConnectionSettingsBuilder;
import io.perfana.client.api.TestContext;
import io.perfana.client.api.TestContextBuilder;
import io.perfana.client.domain.TestRunConfigJson;
import io.perfana.client.domain.TestRunConfigKeyValue;
import io.perfana.client.exception.PerfanaAssertResultsException;
import io.perfana.client.exception.PerfanaAssertionsAreFalse;
import io.perfana.client.exception.PerfanaClientException;
import io.perfana.client.exception.PerfanaClientRuntimeException;
import io.perfana.eventscheduler.api.*;
import io.perfana.eventscheduler.api.message.EventMessage;
import io.perfana.eventscheduler.api.message.EventMessageBus;
import io.perfana.eventscheduler.api.message.EventMessageReceiver;
import io.perfana.eventscheduler.exception.handler.KillSwitchException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerfanaEvent extends EventAdapter<PerfanaEventContext> {

    private static final String CLASSNAME = PerfanaEvent.class.getName();
    private final String eventName;

    private final TestContext perfanaTestContext;

    private final EventMessageBus messageBus;

    private final Map<String,String> receivedVariables = new ConcurrentHashMap<>();

    private final PerfanaClient perfanaClient;

    private String abortDetailMessage = null;
    // save some state to do the status check
    private EventCheck eventCheck;

    PerfanaEvent(PerfanaEventContext context, EventMessageBus messageBus, EventLogger logger) {
        super(context, messageBus, logger);
        this.eventCheck = new EventCheck(context.getName(), CLASSNAME, EventStatus.UNKNOWN, "No known result yet. Try again some time later.");
        this.eventName = context.getName();
        this.perfanaTestContext = createPerfanaTestContext(context);
        this.messageBus = messageBus;

        this.perfanaClient = createPerfanaClient(context, perfanaTestContext, logger);

        EventMessageReceiver eventMessageReceiver = message -> {
            // a test-run-config message
            if (message.getVariables().getOrDefault("message-type", "").equals("test-run-config")) {
                logger.debug("received test-run-config message from " + message.getPluginName());
                addTestRunConfig(message);
            }
            else if (!message.getVariables().isEmpty()) {
                logger.debug("received variables from " + message.getPluginName() + ": " + message.getVariables());
                receivedVariables.putAll(message.getVariables());
            }
        };
        this.messageBus.addReceiver(eventMessageReceiver);
    }

    private void addTestRunConfig(EventMessage message) {

        Map<String, String> variables = message.getVariables();

        String output = variables.get("output");
        String tags = variables.getOrDefault("tags", "");

        if (output.equals("key")) {

            TestRunConfigKeyValue.TestRunConfigKeyValueBuilder testRunConfig = TestRunConfigKeyValue.builder()
                    .testRunId(perfanaTestContext.getTestRunId())
                    .application(perfanaTestContext.getSystemUnderTest())
                    .testEnvironment(perfanaTestContext.getTestEnvironment())
                    .testType(perfanaTestContext.getWorkload())
                    .key(variables.get("key"))
                    .value(message.getMessage());

            Arrays.stream(tags.split(",")).forEach(testRunConfig::tag);

            perfanaClient.addTestRunConfigKeyValue(testRunConfig.build());

        }
        else if (output.equals("json")) {

            TestRunConfigJson.TestRunConfigJsonBuilder testRunConfigJson = TestRunConfigJson.builder()
                    .testRunId(perfanaTestContext.getTestRunId())
                    .application(perfanaTestContext.getSystemUnderTest())
                    .testEnvironment(perfanaTestContext.getTestEnvironment())
                    .testType(perfanaTestContext.getWorkload())
                    .json(message.getMessage());

            String excludes = variables.getOrDefault("excludes", "");
            String includes = variables.getOrDefault("includes", "");

            Arrays.stream(excludes.split(",")).forEach(testRunConfigJson::excludeItem);
            Arrays.stream(includes.split(",")).forEach(testRunConfigJson::includeItem);

            Arrays.stream(tags.split(",")).forEach(testRunConfigJson::tag);

            perfanaClient.addTestRunConfigJson(testRunConfigJson.build());
        }
        else {
            logger.error("received test-run-config message with unexpected output type: " + output);
        }
    }

    private static PerfanaClient createPerfanaClient(
            PerfanaEventContext eventContext,
            TestContext perfanaTestContext,
            EventLogger logger) {

        PerfanaConnectionSettings settings = new PerfanaConnectionSettingsBuilder()
                .setPerfanaUrl(eventContext.getPerfanaUrl())
                .setApiKey(eventContext.getApiKey())
                .build();

        PerfanaClientBuilder builder = new PerfanaClientBuilder()
                .setLogger(new PerfanaClientEventLogger(logger))
                .setTestContext(perfanaTestContext)
                .setPerfanaConnectionSettings(settings)
                .setAssertResultsEnabled(eventContext.isAssertResultsEnabled());

        return builder.build();
    }

    @Override
    public void beforeTest() {
        sendTestRunConfig();
    }

    private void sendTestRunConfig() {
        Map<String, String> configLines = createTestRunConfigLines();
        configLines.forEach((name, value) -> sendKeyValueMessage(name, value, eventContext.getName(), "perfana-java-client"));
    }

    private Map<String, String> createTestRunConfigLines() {
        String prefix = "event." + eventContext.getName() + ".";
        Map<String, String> lines = new HashMap<>();
        lines.put(prefix + "perfanaUrl", eventContext.getPerfanaUrl());
        lines.put(prefix + "isAssertResultsEnabled", String.valueOf(eventContext.isAssertResultsEnabled()));
        lines.put(prefix + "scheduleScript", eventContext.getScheduleScript());
        lines.put(prefix + "variables", String.valueOf(eventContext.getVariables()));
        lines.put(prefix + "apiKey", hashSecret(eventContext.getApiKey()));
        return lines;
    }

    private String hashSecret(String secretToHash) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(secretToHash.getBytes());
            return "(hashed-secret)" + new String(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algo not found", e);
            return "(hashed-secret)" + "(sorry, no algorithm found)";
        }
    }

    private void sendKeyValueMessage(String key, String value, String pluginName, String tags) {

        EventMessage.EventMessageBuilder messageBuilder = EventMessage.builder();

        messageBuilder.variable("message-type", "test-run-config");
        messageBuilder.variable("output", "key");
        messageBuilder.variable("tags", tags);

        messageBuilder.variable("key", key);
        messageBuilder.message(value);

        this.eventMessageBus.send(messageBuilder.pluginName(pluginName).build());
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
        } catch (PerfanaAssertResultsException e) {
            logger.error("Perfana check results failed: " + e.getMessage());
            eventCheck = new EventCheck(eventName, CLASSNAME, EventStatus.FAILURE, "Perfana check results failed: " + e.getMessage());
        } catch (PerfanaClientException e) {
            logger.error("Perfana check results failed.", e);
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

        io.perfana.eventscheduler.api.config.TestContext testContext = context.getTestContext();

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
