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
package io.perfana.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.perfana.client.api.PerfanaCaller;
import io.perfana.client.api.PerfanaClientLogger;
import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.TestContext;
import io.perfana.client.domain.*;
import io.perfana.client.exception.PerfanaAssertionsAreFalse;
import io.perfana.client.exception.PerfanaClientException;
import io.perfana.client.exception.PerfanaClientRuntimeException;
import nl.stokpop.eventscheduler.exception.handler.AbortSchedulerException;
import nl.stokpop.eventscheduler.exception.handler.KillSwitchException;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static java.net.HttpURLConnection.*;

public final class PerfanaClient implements PerfanaCaller {

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    private final PerfanaClientLogger logger;

    private final TestContext context;
    private final PerfanaConnectionSettings settings;
    
    private final boolean assertResultsEnabled;

    private static final ObjectReader perfanaBenchmarkReader;
    private static final ObjectReader messageReader;
    private static final ObjectReader perfanaTestReader;
    private static final ObjectWriter perfanaMessageWriter;
    private static final ObjectWriter perfanaEventWriter;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        perfanaBenchmarkReader = objectMapper.reader().forType(Benchmark.class);
        messageReader = objectMapper.reader().forType(Message.class);
        perfanaTestReader = objectMapper.reader().forType(PerfanaTest.class);
        perfanaMessageWriter = objectMapper.writer().forType(PerfanaMessage.class);
        perfanaEventWriter = objectMapper.writer().forType(PerfanaEvent.class);
    }

    PerfanaClient(TestContext context, PerfanaConnectionSettings settings,
                  boolean assertResultsEnabled, PerfanaClientLogger logger) {
        this.context = context;
        this.settings = settings;
        this.assertResultsEnabled = assertResultsEnabled;
        this.logger = logger;
    }

    public void callPerfanaTestEndpoint(TestContext context, boolean completed) throws KillSwitchException {
        callPerfanaTestEndpoint(context, completed, Collections.emptyMap());
    }

    @Override
    public void callPerfanaTestEndpoint(TestContext context, boolean completed, Map<String, String> extraVariables) throws KillSwitchException {
        String json = perfanaMessageToJson(context, completed, extraVariables);

        Request request = createRequest("/api/test", json);

        try (Response result = client.newCall(request).execute()) {

            logger.debug("test endpoint result: " + result);

            final int responseCode = result.code();
            final ResponseBody responseBody = result.body();

            if (responseCode == HTTP_UNAUTHORIZED) {
                throw new AbortSchedulerException("Abort due to: not authorized (401) for [" + request + "]");
            } else if (responseCode == HTTP_BAD_REQUEST) {
                if (responseBody != null) {
                    Message message = messageReader.readValue(responseBody.string());
                    throw new AbortSchedulerException(message.getMessage());
                } else {
                    logger.error("No response body in test endpoint result: " + result);
                }
            } else {
                if (responseBody != null) {
                    // only do the abort check for the keep alive calls, completed is final call
                    if (!completed) {
                        PerfanaTest test = perfanaTestReader.readValue(responseBody.string());
                        if (test.isAbort()) {
                            String message = test.getAbortMessage();
                            logger.info("abort requested by Perfana! Reason: '" + message + "'");
                            throw new KillSwitchException(message);
                        }
                    }
                } else {
                    logger.error("No response body in test endpoint result: " + result);
                }
            }
        } catch (IOException e) {
            logger.error("failed to call Perfana test endpoint: " + e.getMessage());
        }
    }


    @NotNull
    private Request createRequest(String endPoint) {
        return createRequest(endPoint, null);
    }

    private Request createRequest(@NotNull String endpoint, String json) {
        logger.debug("call to endpoint: " + endpoint + (json != null ? " with json: " + json : ""));

        String url = PerfanaUtils.addSlashIfNeeded(settings.getPerfanaUrl(), endpoint);

        Request.Builder requestBuilder = new Request.Builder()
            .url(url);

        if (json == null) {
            requestBuilder.get();
        }
        else {
            RequestBody body = RequestBody.create(json, JSON);
            requestBuilder.post(body);
        }

        if (settings.getApiKey() != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + settings.getApiKey());
        }

        return requestBuilder.build();
    }

    @Override
    public void callPerfanaEvent(TestContext context, String eventTitle, String eventDescription) {
        logger.info("add Perfana event: " + eventDescription);
        String json = perfanaEventToJson(context, eventTitle, eventDescription);
        try {
            String result = post("/api/events", json);
            logger.debug("result: " + result);
        } catch (IOException e) {
            logger.error("failed to call Perfana event endpoint: " + e.getMessage());
        }
    }

    private String post(String endpoint, String json) throws IOException {
        Request request = createRequest(endpoint, json);
        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            final int responseCode = response.code();
            if (responseCode == HTTP_UNAUTHORIZED) {
                logger.warn("ignoring: not authorised (401) to post to [" + endpoint + "]");
            }
            if (!response.isSuccessful()) {
                logger.warn("POST was not successful: " + response + " for request: " + request + " and body: " + json);
            }
            return responseBody == null ? "null" : responseBody.string();
        }
    }

    public static String perfanaMessageToJson(TestContext context, boolean completed, Map<String, String> extraVariables) {

        PerfanaMessage.PerfanaMessageBuilder perfanaMessageBuilder = PerfanaMessage.builder()
            .testRunId(context.getTestRunId())
            .workload(context.getWorkload())
            .testEnvironment(context.getTestEnvironment())
            .systemUnderTest(context.getSystemUnderTest())
            .version(context.getVersion())
            .cibuildResultsUrl(context.getCIBuildResultsUrl())
            .rampUp(String.valueOf(context.getRampupTime().getSeconds()))
            .duration(String.valueOf(context.getPlannedDuration().getSeconds()))
            .completed(completed)
            .annotations(context.getAnnotations())
            .tags(context.getTags());

        context.getVariables().forEach((k,v) -> perfanaMessageBuilder
            .variable(Variable.builder().placeholder(k).value(v).build()));

        extraVariables.forEach((k, v) -> perfanaMessageBuilder
            .variable(Variable.builder().placeholder(k).value(v).build()));

        PerfanaMessage perfanaMessage = perfanaMessageBuilder.build();

        try {
            return perfanaMessageWriter.writeValueAsString(perfanaMessage);
        } catch (JsonProcessingException e) {
            throw new PerfanaClientRuntimeException("Failed to write PerfanaMessage to json: " + perfanaMessage, e);
        }
    }

    private String perfanaEventToJson(TestContext context, String eventTitle, String eventDescription) {

        PerfanaEvent event = PerfanaEvent.builder()
            .systemUnderTest(context.getSystemUnderTest())
            .testEnvironment(context.getTestEnvironment())
            .title(eventTitle)
            .description(eventDescription)
            .tag(context.getWorkload())
            .build();

        try {
            return perfanaEventWriter.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new PerfanaClientRuntimeException("Unable to transform PerfanaEvent to json", e);
        }
    }

    /**
     * Call asserts for this test run.
     * @return string such as "All configured checks are OK:
     *     https://perfana:4000/requirements/123
     *     https://perfana:4000/benchmarkBaseline/123
     *     https://perfana:4000/benchmarkPrevious/123"
     * @throws PerfanaClientException when call fails
     */
    private String callCheckAsserts() throws PerfanaClientException {
        // example: https://perfana-url/api/benchmark-results/DASHBOARD/NIGHTLY/TEST-RUN-831

        // response example: {
        //     "requirements":{"result":true,"deeplink":"https://perfana:4000/requirements/123"},
        //     "benchmarkPreviousTestRun":{"result":true,"deeplink":"https://perfana:4000/benchmarkPrevious/123"},
        //     "benchmarkBaselineTestRun":{"result":true,"deeplink":"https://perfana:4000/benchmarkBaseline/123"}
        // }

        String endPoint;
        try {
            endPoint = String.join("/",  "/api", "benchmark-results", encodeForURL(context.getSystemUnderTest()), encodeForURL(context.getTestRunId()));
        } catch (UnsupportedEncodingException e) {
            throw new PerfanaClientException("cannot encode Perfana url.", e);
        }
        
        Request request = createRequest(endPoint);

        final int maxRetryCount = settings.getRetryMaxCount();
        final long sleepDurationMillis = settings.getRetryDuration().toMillis();

        int retryCount = 0;
        String assertions = null;
        boolean assertionsAvailable = false;
        boolean checksSpecified = false;

        while (!assertionsAvailable && retryCount++ < maxRetryCount) {
            try (Response response = client.newCall(request).execute()) {
                ResponseBody responseBody = response.body();

                final int responseCode = response.code();
                if (responseCode == HTTP_OK) {
                    assertions = (responseBody == null) ? "null" : responseBody.string();
                    assertionsAvailable = true;
                    checksSpecified = true;
                } else if (responseCode == HTTP_NO_CONTENT) {
                    // no check specified
                    assertionsAvailable = true;
                    checksSpecified = false;
                } else if (responseCode == HTTP_BAD_REQUEST) {
                    // something went wrong
                    throw new PerfanaClientException("Something went wrong while evaluating the test run [" + context.getTestRunId() + "]");
                } else if (responseCode == HTTP_NOT_FOUND) {
                    // test run not found
                    throw new PerfanaClientException("Test run not found [" + context.getTestRunId() + "]");
                }  else if (responseCode == HTTP_ACCEPTED) {
                    //  evaluation in progress
                    logger.info(String.format("Trying to get test run check results at %s, attempt [%d/%d]. Returncode [%d]: Test run evaluation in progress ...",
                        endPoint, retryCount, maxRetryCount, responseCode));
                } else if (responseCode == HTTP_UNAUTHORIZED) {
                    throw new PerfanaClientException("Not authorized (401) for [" + endPoint + "]");
                } else {
                    throw new PerfanaClientException("No action defined for dealing with http code (" + responseCode + ") for [" + endPoint + "]");
                }
            } catch (IOException e) {
                throw new PerfanaClientException("Exception while trying to get test run check results at [" + endPoint + "]", e);
            }

            if (!assertionsAvailable) {
                sleep(sleepDurationMillis);
            }
        }
        if (!assertionsAvailable) {
            logger.warn("Failed to get test run check results at [" + endPoint + "], maximum attempts reached!");
            throw new PerfanaClientException("Failed to get test run check results at [" + endPoint + "], maximum attempts reached!");
        }
        return checksSpecified ? assertions : null;
    }

    private void sleep(long sleepDurationMillis) {
        try {
            Thread.sleep(sleepDurationMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String encodeForURL(String testRunId) throws UnsupportedEncodingException {
        return URLEncoder.encode(testRunId, "UTF-8").replaceAll("\\+", "%20");
    }

    public String assertResults() throws PerfanaClientException, PerfanaAssertionsAreFalse {

        if (!assertResultsEnabled) {
            String message = "Perfana assert results is not enabled and will not be checked.";
            logger.info(message);
            return message;
        }

        final String assertions = callCheckAsserts();
        if (assertions == null) {
            // No checks have specified
            return "No checks have been specified for this test run! Set assertResults property to false or create checks for key metrics";
        }

        Benchmark benchmark;
        try {
            benchmark = perfanaBenchmarkReader.readValue(assertions);
        } catch (IOException e) {
            throw new PerfanaClientRuntimeException("Unable to parse benchmark message: " + assertions, e);
        }
        Optional<Result> baseline = Optional.ofNullable(benchmark.getBenchmarkBaselineTestRun());
        Optional<Result> previous = Optional.ofNullable(benchmark.getBenchmarkPreviousTestRun());
        Optional<Result> requirements = Optional.ofNullable(benchmark.getRequirements());

        requirements.ifPresent(r -> logger.info("Requirements: " + r.isResult()));
        baseline.ifPresent(r -> logger.info("Compared to baseline test run: " + r.isResult()));
        previous.ifPresent(r -> logger.info("Compared to previous test run: " + r.isResult()));

        StringBuilder text = new StringBuilder();
        if (assertions.contains("false")) {
            text.append("One or more Perfana assertions are failing: \n");
            requirements.filter(r -> !r.isResult()).ifPresent(r -> text.append("Requirements check failed: ").append(r.getDeeplink()).append("\n"));
            baseline.filter(r -> !r.isResult()).ifPresent(r -> text.append("Comparison check to baseline test run failed: ").append(r.getDeeplink()).append("\n"));
            previous.filter(r -> !r.isResult()).ifPresent(r -> text.append("Comparison check to previous test run failed: ").append(r.getDeeplink()).append("\n"));
            logger.info("Test run has failed checks: " + text);
            throw new PerfanaAssertionsAreFalse(text.toString());
        } else {
            text.append("All configured checks are OK: \n");
            requirements.ifPresent(r -> text.append(r.getDeeplink()).append("\n"));
            baseline.ifPresent(r -> text.append(r.getDeeplink()).append("\n"));
            previous.ifPresent(r -> text.append(r.getDeeplink()));
        }
        return text.toString();
    }

    @Override
    public String toString() {
        return "PerfanaClient [testRunId:" + context.getTestRunId() +
            " workload: " + context.getWorkload() +
            " testEnvironment: " + context.getTestEnvironment() +
            " Perfana url: " + settings.getPerfanaUrl() + "]";
    }

}
