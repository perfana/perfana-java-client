/*
 *    Copyright 2020-2023  Peter Paul Bakker @ perfana.io, Daniel Moll @ perfana.io
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
import io.perfana.client.PerfanaUtils;
import io.perfana.client.api.PerfanaTestContext;
import io.perfana.client.domain.ConfigItem;
import io.perfana.client.domain.TestRunConfigJson;
import io.perfana.client.domain.TestRunConfigKeyValue;
import io.perfana.client.domain.TestRunConfigKeys;
import io.perfana.client.exception.PerfanaAssertResultsException;
import io.perfana.client.exception.PerfanaAssertionsAreFalse;
import io.perfana.client.exception.PerfanaClientException;
import io.perfana.eventscheduler.api.*;
import io.perfana.eventscheduler.api.config.TestContext;
import io.perfana.eventscheduler.api.message.EventMessage;
import io.perfana.eventscheduler.api.message.EventMessageBus;
import io.perfana.eventscheduler.api.message.EventMessageReceiver;
import io.perfana.eventscheduler.exception.handler.KillSwitchException;
import io.perfana.eventscheduler.util.TestRunConfigUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerfanaEvent extends EventAdapter<PerfanaEventContext> {

    private static final String CLASSNAME = PerfanaEvent.class.getName();
    public static final String PLUGIN_NAME = "perfana-java-client";
    private final String eventName;

    private final PerfanaTestContext perfanaTestContext;

    private final EventMessageBus messageBus;

    private final Map<String,String> receivedVariables = new ConcurrentHashMap<>();

    private final PerfanaClient perfanaClient;

    private String abortDetailMessage = null;
    // save some state to do the status check
    private EventCheck eventCheck;

    PerfanaEvent(PerfanaEventContext context, TestContext testContext,EventMessageBus messageBus, EventLogger logger) {
        super(context, testContext, messageBus, logger);
        this.eventCheck = new EventCheck(context.getName(), CLASSNAME, EventStatus.UNKNOWN, "No known result yet. Try again some time later.");
        this.eventName = context.getName();
        this.perfanaTestContext = PerfanaUtils.createPerfanaTestContext(context, testContext);
        this.messageBus = messageBus;

        this.perfanaClient = PerfanaUtils.createPerfanaClient(context, perfanaTestContext, logger);

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

        switch (output) {
            case "key":

                TestRunConfigKeyValue.TestRunConfigKeyValueBuilder testRunConfig = TestRunConfigKeyValue.builder()
                        .testRunId(perfanaTestContext.getTestRunId())
                        .application(perfanaTestContext.getSystemUnderTest())
                        .testEnvironment(perfanaTestContext.getTestEnvironment())
                        .testType(perfanaTestContext.getWorkload())
                        .key(variables.get("key"))
                        .value(replaceNullWithEmptyString(message.getMessage()));

                Arrays.stream(tags.split(",")).forEach(testRunConfig::tag);

                perfanaClient.addTestRunConfigKeyValue(testRunConfig.build());

                break;
            case "json":

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
                break;
            case "keys":
                TestRunConfigKeys.TestRunConfigKeysBuilder keysBuilder = TestRunConfigKeys.builder()
                        .testRunId(perfanaTestContext.getTestRunId())
                        .application(perfanaTestContext.getSystemUnderTest())
                        .testEnvironment(perfanaTestContext.getTestEnvironment())
                        .testType(perfanaTestContext.getWorkload());

                Arrays.stream(tags.split(",")).forEach(keysBuilder::tag);

                String keyValuePairsString = message.getMessage();

                // -1 to keep empty strings in split
                List<String> keyValuePairs = Arrays.asList(keyValuePairsString.split(TestRunConfigUtil.MESSAGE_KEY_VALUE_DELIMITER, -1));

                if (keyValuePairs.size() % 2 != 0) {
                    logger.error("skip send of test config key value pairs: received string with uneven number of key-value items: " + keyValuePairs.size());
                } else {
                    for (int i = 0; i < keyValuePairs.size(); i = i + 2) {
                        keysBuilder.configItem(new ConfigItem(keyValuePairs.get(i), keyValuePairs.get(i + 1)));
                    }
                    perfanaClient.addTestRunConfigKeys(keysBuilder.build());
                }
                break;
            default:
                logger.error("received test-run-config message with unexpected output type: " + output);
                break;
        }
    }

    private String replaceNullWithEmptyString(String text) {
        return text == null ? "" : text;
    }

    @Override
    public void beforeTest() {
        sendTestRunConfig();
    }

    private void sendTestRunConfig() {
        Map<String, String> configKeyValues = createTestRunConfigKeyValues();
        EventMessage message = TestRunConfigUtil.createTestRunConfigMessageKeys(eventContext.getName(), configKeyValues, PLUGIN_NAME);
        this.eventMessageBus.send(message);
    }

    private Map<String, String> createTestRunConfigKeyValues() {
        String prefix = "event." + eventContext.getName() + ".";
        Map<String, String> lines = new HashMap<>();
        lines.put(prefix + "perfanaUrl", eventContext.getPerfanaUrl());
        lines.put(prefix + "isAssertResultsEnabled", String.valueOf(eventContext.isAssertResultsEnabled()));
        lines.put(prefix + "scheduleScript", eventContext.getScheduleScript());
        lines.put(prefix + "variables", String.valueOf(eventContext.getVariables()));
        return lines;
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

}
