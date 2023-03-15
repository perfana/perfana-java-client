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
package io.perfana.client;

import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.PerfanaConnectionSettingsBuilder;
import io.perfana.client.api.TestContext;
import io.perfana.client.api.TestContextBuilder;
import io.perfana.client.exception.PerfanaClientRuntimeException;
import io.perfana.event.PerfanaClientEventLogger;
import io.perfana.event.PerfanaEventContext;
import io.perfana.eventscheduler.api.EventLogger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PerfanaUtils {

    private PerfanaUtils() {}

    public static int parseInt(String variableName, String numberString, int defaultValue) {
        int time;
        try {
            time = Integer.parseInt(numberString);
        } catch (NumberFormatException e) {
            System.err.printf("unable to parse value of [%s=%s]: using default value [%d]. Error message: %s.%n", variableName, numberString, defaultValue, e.getMessage());
            time = defaultValue;
        }
        return time;
    }

    public static boolean hasValue(String variable) {
        return variable != null && !variable.trim().isEmpty();
    }

    public static int countOccurrences(String search, String text) {
        Matcher matcher = Pattern.compile(search).matcher(text);
        int count = 0;
        while (matcher.find()) { count = count + 1; }
        return count;
    }

    public static List<String> splitAndTrim(String text, String separator) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(text.split(separator))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static String addSlashIfNeeded(String baseUrl, String endpoint) {
        String cleanUrl;
        if (baseUrl == null) {
            cleanUrl = "";
        } else {
            cleanUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        }
        String slashEndpoint;
        if (endpoint == null) {
            slashEndpoint = "";
        } else {
            slashEndpoint = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        }
        return  (cleanUrl + slashEndpoint).replaceAll("/+", "/");
    }

    public static PerfanaClient createPerfanaClient(
            PerfanaEventContext eventContext,
            TestContext testContext,
            EventLogger logger) {

        PerfanaConnectionSettings settings = new PerfanaConnectionSettingsBuilder()
                .setPerfanaUrl(eventContext.getPerfanaUrl())
                .setApiKey(eventContext.getApiKey())
                .setRetryMaxCount(eventContext.getRetryCount())
                .setRetryTimeSeconds(String.valueOf(eventContext.getRetryDelaySeconds()))
                .build();

        PerfanaClientBuilder builder = new PerfanaClientBuilder()
                .setLogger(new PerfanaClientEventLogger(logger))
                .setTestContext(testContext)
                .setPerfanaConnectionSettings(settings)
                .setAssertResultsEnabled(eventContext.isAssertResultsEnabled());

        return builder.build();
    }

    public static TestContext createPerfanaTestContext(PerfanaEventContext context) {

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
