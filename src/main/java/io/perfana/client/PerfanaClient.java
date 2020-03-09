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

import com.jayway.jsonpath.*;
import io.perfana.client.api.PerfanaCaller;
import io.perfana.client.api.PerfanaClientLogger;
import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.TestContext;
import io.perfana.client.exception.PerfanaAssertionsAreFalse;
import io.perfana.client.exception.PerfanaClientException;
import io.perfana.client.exception.PerfanaClientRuntimeException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import nl.stokpop.eventscheduler.exception.KillSwitchException;
import okhttp3.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.*;

public final class PerfanaClient implements PerfanaCaller {

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    private final PerfanaClientLogger logger;

    private final TestContext context;
    private final PerfanaConnectionSettings settings;
    
    private final boolean assertResultsEnabled;

    PerfanaClient(TestContext context, PerfanaConnectionSettings settings,
                  boolean assertResultsEnabled, PerfanaClientLogger logger) {
        this.context = context;
        this.settings = settings;
        this.assertResultsEnabled = assertResultsEnabled;
        this.logger = logger;
    }

    @Override
    public void callPerfanaTestEndpoint(TestContext context, boolean completed) throws KillSwitchException  {
        String json = perfanaMessageToJson(context, completed);
        RequestBody body = RequestBody.create(json, JSON);

        String testUrl = settings.getPerfanaUrl() + "/test";
        logger.debug("call to endpoint: " + testUrl + " with json: " + json);
        Configuration config = Configuration.defaultConfiguration()
                .addOptions(Option.SUPPRESS_EXCEPTIONS);

        ParseContext parseContext = JsonPath.using(config);

        Request request = new Request.Builder()
                .url(testUrl)
                .post(body)
                .build();

        try (Response result = client.newCall(request).execute()) {

            logger.debug("test endpoint result: " + result);

            final int reponseCode = result.code();
            final ResponseBody responseBody = result.body();
            if (reponseCode == HTTP_BAD_REQUEST) {

                DocumentContext doc = parseContext.parse(responseBody);
                String message = doc.read("$.message");

                throw new PerfanaClientRuntimeException(message);

            } else {

                if (responseBody != null) {
                    String bodyAsString = responseBody.string();
                    Boolean abort = parseResultForAbort(bodyAsString);
                    if (abort != null && abort) {
                        String message = parseResultForAbortMessage(bodyAsString);
                        logger.info("abort requested by Perfana! Reason: '" + message + "'");
                        throw new KillSwitchException(message);
                    }
                }
                else {
                    logger.error("No response body in test endpoint result: " + result);
                }
            }
        } catch (IOException e) {
            logger.error("failed to call Perfana: " + e.getMessage());
        }
    }

    private String parseResultForAbortMessage(String result) {
        DocumentContext documentContext = JsonPath.parse(result);
        String abortMessage;
        try {
            abortMessage = documentContext.read("abortMessage");
        } catch (PathNotFoundException e) {
            logger.warn("No 'abortMessage' field found in json [" + result + "]");
            abortMessage = "Unknown: no 'abortMessage' found.";
        }
        return abortMessage;
    }

    @Nullable
    private Boolean parseResultForAbort(String result) {
        DocumentContext documentContext = JsonPath.parse(result);
        Boolean abort;
        try {
            abort = documentContext.read("abort");
        } catch (PathNotFoundException e) {
            logger.warn("No 'abort' field found in json [" + result + "]");
            abort = null;
        }
        return abort;
    }

    @Override
    public void callPerfanaEvent(TestContext context, String eventTitle, String eventDescription) {
        logger.info("add Perfana event: " + eventDescription);
        String json = perfanaEventToJson(context, eventTitle, eventDescription);
        String eventsUrl = settings.getPerfanaUrl() + "/events";
        logger.debug("add perfana event to endpoint: " + eventsUrl + " with json: " + json);
        try {
            String result = post(eventsUrl, json);
            logger.debug("result: " + result);
        } catch (IOException e) {
            logger.error("failed to call perfana: " + e.getMessage());
        }
    }

    private String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (!response.isSuccessful()) {
                logger.warn("POST was not successful: " + response + " for request: " + request + " and body: " + json);
            }
            return responseBody == null ? "null" : responseBody.string();
        }
    }

    public static String perfanaMessageToJson(TestContext context, boolean completed) {

        JSONObject json = new JSONObject();

        /* If variables parameter exists add them to the json */
        Map<String, String> variables = context.getVariables();
        if(variables != null && !variables.isEmpty()) {
            JSONArray variablesArray = new JSONArray();
            variables.forEach((key, value) -> variablesArray.add(createVariables(key, value)));
            json.put("variables", variablesArray);
        }

        /* If tags parameter exists add them to the json */
        List<String> tags = context.getTags();
        if(tags != null) {
            JSONArray tagsArray = new JSONArray();
            tagsArray.addAll(tags);
            json.put("tags", tagsArray);
        }

        /* If annotations are passed add them to the json */
        String annotations = context.getAnnotations();
        if(annotations != null && !annotations.isEmpty()){
            json.put("annotations", annotations);
        }

        json.put("testRunId", context.getTestRunId());
        json.put("workload", context.getWorkload());
        json.put("environment", context.getEnvironment());
        json.put("systemUnderTest", context.getSystemUnderTest());
        json.put("version", context.getVersion());
        json.put("CIBuildResultsUrl", context.getCIBuildResultsUrl());
        json.put("rampUp", String.valueOf(context.getRampupTime().getSeconds()));
        json.put("duration", String.valueOf(context.getPlannedDuration().getSeconds()));
        json.put("completed", completed);

        return json.toString();
    }

    private static JSONObject createVariables(String name, String value) {
        JSONObject variables = new JSONObject();
        variables.put("placeholder", name);
        variables.put("value", value);
        return variables;
    }

    private String perfanaEventToJson(TestContext context, String eventTitle, String eventDescription) {
        JSONObject json = new JSONObject();

        json.put("systemUnderTest", context.getSystemUnderTest());
        json.put("environment", context.getEnvironment());
        json.put("title", eventTitle);
        json.put("description", eventDescription);

        JSONArray tags = new JSONArray();
        tags.add(context.getWorkload());
        json.put("tags", tags);
        
        return json.toJSONString();
    }

    /**
     * Call asserts for this test run.
     * @return json string such as {"meetsRequirement":true,"benchmarkResultPreviousOK":true,"benchmarkResultFixedOK":true}
     * @throws PerfanaClientException when call fails
     */
    private String callCheckAsserts() throws PerfanaClientException {
        // example: https://perfana-url/benchmarks/DASHBOARD/NIGHTLY/TEST-RUN-831
        String url;
        try {
            url = String.join("/", settings.getPerfanaUrl(), "get-benchmark-results", encodeForURL(context.getSystemUnderTest()), encodeForURL(context.getTestRunId()));
        } catch (UnsupportedEncodingException e) {
            throw new PerfanaClientException("cannot encode perfana url.", e);
        }
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        final int maxRetryCount = settings.getRetryMaxCount();
        final long sleepDurationMillis = settings.getRetryDuration().toMillis();

        int retryCount = 0;
        String assertions = null;
        boolean assertionsAvailable = false;
        boolean checksSpecified = false;

        while (!assertionsAvailable && retryCount++ < maxRetryCount) {
            try (Response response = client.newCall(request).execute()) {
                ResponseBody responseBody = response.body();

                final int reponseCode = response.code();
                if (reponseCode == HTTP_OK) {
                    assertions = (responseBody == null) ? "null" : responseBody.string();
                    assertionsAvailable = true;
                    checksSpecified = true;
                } else if (reponseCode == HTTP_NO_CONTENT) {
                    // No check specified
                    assertionsAvailable = true;
                    checksSpecified = false;
                } else if (reponseCode == HTTP_BAD_REQUEST) {
                    // something went wrong
                    throw new PerfanaClientException("Something went wrong while evaluating the test run");
                } else if (reponseCode == HTTP_NOT_FOUND) {
                    // test run not found
                    throw new PerfanaClientException("Test run not found");
                }  else if (reponseCode == HTTP_ACCEPTED) {
                    //  evaluation in progress
                    logger.info(
                    String.format("Trying to get test run check results at %s, attempt [%d/%d]. Returncode [%d]: Test run evaluation in progress ...",
                    url, retryCount, maxRetryCount, reponseCode));
                }

            } catch (IOException e) {
                throw new PerfanaClientException("Exception while trying to get test run check results at [" + url + "]", e);
            }
            if (!assertionsAvailable) {
                sleep(sleepDurationMillis);
            }
        }
        if (!assertionsAvailable) {

            if(!checksSpecified) {
                return null;
            }
            else {
                logger.warn("Failed to get test run check results at [" + url + "], maximum attempts reached!");
                throw new PerfanaClientException("Failed to get test run check results at [" + url + "], maximum attempts reached!");
            }

        }
        return assertions;
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

        Configuration config = Configuration.defaultConfiguration()
                .addOptions(Option.SUPPRESS_EXCEPTIONS);

        ParseContext parseContext = JsonPath.using(config);
        DocumentContext doc = parseContext.parse(assertions);

        Boolean benchmarkBaselineTestRunResult = doc.read("$.benchmarkBaselineTestRun.result");
        String benchmarkBaselineTestRunDeeplink = doc.read("$.benchmarkBaselineTestRun.deeplink");
        Boolean benchmarkPreviousTestRunResult = doc.read("$.benchmarkPreviousTestRun.result");
        String benchmarkPreviousTestRunDeeplink = doc.read("$.benchmarkPreviousTestRun.deeplink");
        Boolean requirementsResult = doc.read("$.requirements.result");
        String requirementsDeeplink = doc.read("$.requirements.deeplink");

        logger.info("Compared to baseline test run: " + benchmarkBaselineTestRunResult);
        logger.info("Compared to previous test run: " + benchmarkPreviousTestRunResult);
        logger.info("Requirements: " + requirementsResult);

        StringBuilder text = new StringBuilder();
        if (assertions.contains("false")) {

            text.append("One or more Perfana assertions are failing: \n");
            if (hasFailed(requirementsResult)) {
                text.append("Requirements check failed: ").append(requirementsDeeplink).append("\n");
            }
            if (hasFailed(benchmarkPreviousTestRunResult)) {
                text.append("Comparison check to previous test run failed: ").append(benchmarkPreviousTestRunDeeplink).append("\n");
            }
            if (hasFailed(benchmarkBaselineTestRunResult)) {
                text.append("Comparison check to baseline test run failed: ").append(benchmarkBaselineTestRunDeeplink);
            }

            logger.info("Test run has failed checks: " + text);

            throw new PerfanaAssertionsAreFalse(text.toString());
        }
        else {

            text.append("All configured checks are OK: \n");
            if (requirementsResult) {
                text.append(requirementsDeeplink).append("\n");
            }
            if (hasSucceeded(benchmarkPreviousTestRunResult)) {
                text.append(benchmarkPreviousTestRunDeeplink).append("\n");
            }
            if (hasSucceeded(benchmarkBaselineTestRunResult)) {
                text.append(benchmarkBaselineTestRunDeeplink);
            }

        }
        return text.toString();
    }

    private static boolean hasSucceeded(Boolean testResult) {
        return testResult != null && testResult;
    }

    private static boolean hasFailed(Boolean testResult) {
        return testResult != null && !testResult;
    }

    @Override
    public String toString() {
        return "PerfanaClient [testRunId:" + context.getTestRunId() +
            " workload: " + context.getWorkload() +
            " environment: " + context.getEnvironment() +
            " Perfana url: " + settings.getPerfanaUrl() + "]";
    }
}
