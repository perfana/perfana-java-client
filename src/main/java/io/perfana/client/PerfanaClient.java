package io.perfana.client;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import io.perfana.client.api.PerfanaCaller;
import io.perfana.client.api.PerfanaClientLogger;
import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.TestContext;
import io.perfana.client.exception.PerfanaAssertionsAreFalse;
import io.perfana.client.exception.PerfanaClientException;
import io.perfana.event.PerfanaEventBroadcaster;
import io.perfana.event.PerfanaEventProperties;
import io.perfana.event.ScheduleEvent;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

public final class PerfanaClient implements PerfanaCaller {

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    private final PerfanaClientLogger logger;

    private final TestContext context;
    private final PerfanaConnectionSettings settings;
    
    private final boolean assertResultsEnabled;

    private final PerfanaEventBroadcaster broadcaster;
    private final PerfanaEventProperties eventProperties;
    private final List<ScheduleEvent> scheduleEvents;

    private PerfanaExecutorEngine executorEngine;

    private boolean isSessionStopped = false;

    PerfanaClient(TestContext context, PerfanaConnectionSettings settings,
                  boolean assertResultsEnabled, PerfanaEventBroadcaster broadcaster,
                  PerfanaEventProperties eventProperties,
                  List<ScheduleEvent> scheduleEvents, PerfanaClientLogger logger) {
        this.context = context;
        this.settings = settings;
        this.assertResultsEnabled = assertResultsEnabled;
        this.eventProperties = eventProperties;
        this.broadcaster = broadcaster;
        this.scheduleEvents = scheduleEvents;
        this.logger = logger;
    }

    /**
     * Start a Perfana test session.
     */
    public void startSession() {
        logger.info("Perfana start session");
        isSessionStopped = false;

        executorEngine = new PerfanaExecutorEngine(logger);

        broadcaster.broadcastBeforeTest(context, eventProperties);

        executorEngine.startKeepAliveThread(this, context, settings, broadcaster, eventProperties);
        executorEngine.startCustomEventScheduler(this, context, scheduleEvents, broadcaster, eventProperties);

        callPerfanaEvent(context, "Test start");
    }

    /**
     * Stop a Perfana test session.
     * @throws PerfanaClientException when something fails, e.g. Perfana can not be reached
     * @throws PerfanaAssertionsAreFalse when the Perfana assertion check returned false
     */
    public void stopSession() throws PerfanaClientException, PerfanaAssertionsAreFalse {
        logger.info("Perfana end session.");
        isSessionStopped = true;
        
        executorEngine.shutdownThreadsNow();

        callPerfanaEvent(context, "Test finish");
        broadcaster.broadcastAfterTest(context, eventProperties);

        callPerfanaTestEndpoint(context, true);

        String text = assertResults();
        logger.info(String.format("the assertion text: %s", text));
    }

    public boolean isSessionStopped() {
        return isSessionStopped;
    }

    /**
     * Call to abort this test run.
     * A Perfana abort event is created.
     * No Perfana assertions are checked.
     */
    public void abortSession() {
        logger.warn("Perfana session abort called.");
        isSessionStopped = true;
        
        executorEngine.shutdownThreadsNow();

        callPerfanaEvent(context, "Test aborted");
        broadcaster.broadcastAfterTest(context, eventProperties);
    }

    @Override
    public void callPerfanaTestEndpoint(TestContext context, boolean completed) {
        String json = perfanaMessageToJson(context, completed);
        String testUrl = settings.getPerfanaUrl() + "/test";
        logger.debug(String.format("call to endpoint: %s with json: %s", testUrl, json));
        try {
            String result = post(testUrl, json);
            logger.debug("result: " + result);
        } catch (IOException e) {
            logger.error("failed to call perfana: " + e.getMessage());
        }
    }

    @Override
    public void callPerfanaEvent(TestContext context, String eventDescription) {
        logger.info("add Perfana event: " + eventDescription);
        String json = perfanaEventToJson(context, eventDescription);
        String eventsUrl = settings.getPerfanaUrl() + "/events";
        logger.debug(String.format("add perfana event to endpoint: %s with json: %s", eventsUrl, json));
        try {
            String result = post(eventsUrl, json);
            logger.debug("result: " + result);
        } catch (IOException e) {
            logger.error("failed to call perfana: " + e.getMessage());
        }
    }

    private String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (!response.isSuccessful()) {
                logger.warn(String.format("POST was not successful: %s for request: %s and body: %s", response, request, json));
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
        json.put("testType", context.getTestType());
        json.put("testEnvironment", context.getTestEnvironment());
        json.put("application", context.getApplication());
        json.put("applicationRelease", context.getApplicationRelease());
        json.put("CIBuildResultsUrl", context.getCIBuildResultsUrl());
        json.put("rampUp", String.valueOf(context.getRampupTime().getSeconds()));
        json.put("duration", String.valueOf(context.getPlannedDuration().getSeconds()));
        json.put("completed", completed);

        return json.toJSONString();
    }

    private static JSONObject createVariables(String name, String value) {
        JSONObject variables = new JSONObject();
        variables.put("placeholder", name);
        variables.put("value", value);
        return variables;
    }

    private String perfanaEventToJson(TestContext context, String eventDescription) {
        JSONObject json = new JSONObject();

        json.put("application", context.getApplication());
        json.put("testEnvironment", context.getTestEnvironment());
        json.put("title", context.getTestRunId());
        json.put("description", eventDescription);

        JSONArray tags = new JSONArray();
        tags.add(context.getTestType());
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
            url = String.join("/", settings.getPerfanaUrl(), "get-benchmark-results", encodeForURL(context.getApplication()), encodeForURL(context.getTestRunId()));
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

        while (!assertionsAvailable && retryCount++ < maxRetryCount) {
            try {
                Thread.sleep(sleepDurationMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try (Response response = client.newCall(request).execute()) {
                ResponseBody responseBody = response.body();

                final int reponseCode = response.code();
                if (reponseCode == HTTP_OK) {
                    assertions = (responseBody == null) ? "null" : responseBody.string();
                    assertionsAvailable = true;
                } else if (reponseCode == HTTP_BAD_REQUEST) {
                    // probably no KPI's defined in Perfana, no need to do retries
                    throw new PerfanaClientException("No KPI's have been specified for this test run! Set assertResults property to false or create a KPI");
                } else {
                    logger.info(
                        String.format("failed to retrieve assertions for url [%s] code [%d] retry [%d/%d] %s",
                        url, reponseCode, retryCount, maxRetryCount, "No benchmarks result found, retrying ..."));
                }
            } catch (IOException e) {
                throw new PerfanaClientException(String.format("unable to retrieve assertions for url [%s]", url), e);
            }
        }
        if (!assertionsAvailable) {
            logger.warn(String.format("failed to retrieve assertions for url [%s], no more retries left!", url));
            throw new PerfanaClientException(String.format("unable to retrieve assertions for url [%s]", url));
        }
        return assertions;
    }

    private String encodeForURL(String testRunId) throws UnsupportedEncodingException {
        return URLEncoder.encode(testRunId, "UTF-8").replaceAll("\\+", "%20");
    }

    private String assertResults() throws PerfanaClientException, PerfanaAssertionsAreFalse {

        if (!assertResultsEnabled) {
            String message = "Perfana assert results is not enabled and will not be checked.";
            logger.info(message);
            return message;
        }

        final String assertions = callCheckAsserts();
        if (assertions == null) {
            throw new PerfanaClientException("Perfana assertions could not be checked, received null.");
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

        logger.info(String.format("benchmarkBaselineTestRunResult: %s", benchmarkBaselineTestRunResult));
        logger.info(String.format("benchmarkPreviousTestRunResult: %s", benchmarkPreviousTestRunResult));
        logger.info(String.format("requirementsResult: %s", requirementsResult));

        StringBuilder text = new StringBuilder();
        if (assertions.contains("false")) {

            text.append("One or more Perfana assertions are failing: \n");
            if (hasFailed(requirementsResult)) {
                text.append(String.format("Requirements failed: %s\n", requirementsDeeplink)) ;
            }
            if (hasFailed(benchmarkPreviousTestRunResult)) {
                text.append(String.format("Benchmark to previous test run failed: %s\n", benchmarkPreviousTestRunDeeplink));
            }
            if (hasFailed(benchmarkBaselineTestRunResult)) {
                text.append(String.format("Benchmark to baseline test run failed: %s", benchmarkBaselineTestRunDeeplink));
            }

            logger.info(String.format("assertionText: %s", text));

            throw new PerfanaAssertionsAreFalse(text.toString());
        }
        else {

            text.append("All Perfana assertions are OK: \n");
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
        return String.format("PerfanaClient [testRunId:%s testType:%s testEnv:%s perfanaUrl:%s]",
                context.getTestRunId(), context.getTestType(), context.getTestEnvironment(), settings.getPerfanaUrl());
    }
}
