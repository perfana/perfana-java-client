package io.perfana.client;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
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
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class PerfanaClient {

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    private final String application;
    private final String testType;
    private final String testEnvironment;
    private final String testRunId;
    private final String CIBuildResultsUrl;
    private final String applicationRelease;
    private final String perfanaUrl;
    private final String rampupTimeSeconds;
    private final int plannedDurationInSeconds;
    private final String annotations;
    private final Properties variables;
    private final boolean assertResultsEnabled;
    private Logger logger;

    private ScheduledExecutorService executor;

    PerfanaClient(String application, String testType, String testEnvironment, String testRunId, String CIBuildResultsUrl, String applicationRelease, String rampupTimeInSeconds, String constantLoadTimeInSeconds, String perfanaUrl, String annotations, Properties variables, final boolean assertResultsEnabled) {
        this.application = application;
        this.testType = testType;
        this.testEnvironment = testEnvironment;
        this.testRunId = testRunId;
        this.CIBuildResultsUrl = CIBuildResultsUrl;
        this.applicationRelease = applicationRelease;
        this.rampupTimeSeconds = rampupTimeInSeconds;
        this.plannedDurationInSeconds = parseIntNullIsZero(rampupTimeInSeconds) + parseIntNullIsZero(constantLoadTimeInSeconds);
        this.perfanaUrl = perfanaUrl;
        this.annotations = annotations;
        this.variables = variables;
        this.assertResultsEnabled = assertResultsEnabled;
    }

    public void startSession() {
        logger.info("Perfana start session");

        if (executor != null) {
            throw new RuntimeException("Cannot start perfana session multiple times!");
        }
        final int periodInSeconds = 15;
        logger.info(String.format("Calling Perfana (%s) keep alive every %d seconds.", perfanaUrl, periodInSeconds));

        final PerfanaClient.KeepAliveRunner keepAliveRunner = new PerfanaClient.KeepAliveRunner(this);
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(keepAliveRunner, 0, periodInSeconds, TimeUnit.SECONDS);
    }

    public void stopSession() throws PerfanaClientException {
        logger.info("Perfana end session.");
        if (executor != null) {
            executor.shutdownNow();
        }
        executor = null;
        callPerfana(true);
        assertResults();
    }

    private static int parseIntNullIsZero(final String value) {
        return value == null ? 0 : Integer.valueOf(value);
    }

    void injectLogger(Logger logger) {
        this.logger = logger;
    }

    private void callPerfana(Boolean completed) {
        String json = perfanaJson(application, testType, testEnvironment, testRunId, CIBuildResultsUrl, applicationRelease, rampupTimeSeconds, plannedDurationInSeconds, annotations, variables, completed);
        logger.debug(String.join(" ", "Call to endpoint:", perfanaUrl, "with json:", json));
        try {
            String result = post(perfanaUrl + "/test", json);
            logger.debug("Result: " + result);
        } catch (IOException e) {
            logger.error("Failed to call perfana: " + e.getMessage());
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
            return responseBody == null ? "null" : responseBody.string();
        }
    }

    private String perfanaJson(String application, String testType, String testEnvironment, String testRunId, String CIBuildResultsUrl, String applicationRelease, String rampupTimeSeconds, int plannedDurationInSeconds, String annotations, Properties variables, Boolean completed) {

        JSONObject perfanaJson = new JSONObject();

        /* If variables parameter exists add them to the json */

        if(variables != null && !variables.isEmpty()) {

            JSONArray variablesArrayJson = new JSONArray();

            Enumeration<?> enumeration = variables.propertyNames();
            while (enumeration.hasMoreElements()) {
                String name = (String) enumeration.nextElement();
                String value = (String) variables.get(name);
                JSONObject variablesJson = new JSONObject();
                variablesJson.put("placeholder", name);
                variablesJson.put("value", value);
                variablesArrayJson.add(variablesJson);
            }

            perfanaJson.put("variables", variablesArrayJson);
        }

        /* If annotations are passed add them to the json */

        if(!"".equals(annotations) && annotations != null ){

            perfanaJson.put("annotations", annotations);

        }

        perfanaJson.put("testRunId", testRunId);
        perfanaJson.put("testType", testType);
        perfanaJson.put("testEnvironment", testEnvironment);
        perfanaJson.put("application", application);
        perfanaJson.put("applicationRelease", applicationRelease);
        perfanaJson.put("CIBuildResultsUrl", CIBuildResultsUrl);
        perfanaJson.put("rampUp", rampupTimeSeconds);
        perfanaJson.put("duration", String.valueOf(plannedDurationInSeconds));
        perfanaJson.put("completed", completed);

        return perfanaJson.toJSONString();


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
            url = String.join("/", perfanaUrl, "get-benchmark-results", URLEncoder.encode(application, "UTF-8").replaceAll("\\+", "%20"), URLEncoder.encode(testRunId, "UTF-8").replaceAll("\\+", "%20") );
        } catch (UnsupportedEncodingException e) {
            throw new PerfanaClientException("Cannot encode perfana url.", e);
        }
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        int retryCount = 0;
        final int MAX_RETRIES = 12;
        final long sleepInMillis = 10000;
        String assertions = null;

        boolean assertionsAvailable = false;
        while (retryCount++ < MAX_RETRIES) {
            try {
                Thread.sleep(sleepInMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // ignore
            }
            try (Response response = client.newCall(request).execute()) {
                ResponseBody responseBody = response.body();
                if (response.code() == 200) {
                    assertions = responseBody == null ? "null" : responseBody.string();
                    assertionsAvailable = true;
                    break;
                } else {
                    String message = responseBody == null ? response.message() : responseBody.string();
                    logger.info(String.format("failed to retrieve assertions for url [%s] code [%d] retry [%d/%d] %s", url, response.code(), retryCount, MAX_RETRIES, message));
                }
            } catch (IOException e) {
                throw new PerfanaClientException(String.format("Unable to retrieve assertions for url [%s]", url), e);
            }
        }
        if (!assertionsAvailable) {
            logger.warn(String.format("Failed to retrieve assertions for url [%s], no more retries left!", url));
            throw new PerfanaClientException(String.format("Unable to retrieve assertions for url [%s]", url));
        }
        return assertions;
    }

    public static class KeepAliveRunner implements Runnable {

        private final PerfanaClient client;

        KeepAliveRunner(PerfanaClient client) {
            this.client = client;
        }

        @Override
        public void run() {
            client.callPerfana(false);
        }
    }

    public interface Logger {
        void info(String message);
        void warn(String message);
        void error(String message);
        void debug(String message);
    }
    
    private String assertResults() throws PerfanaClientException {

        if (!assertResultsEnabled) {
            String message = "Perfana assert results not enalbled";
            logger.info(message);
            return message;
        }

        final String assertions = callCheckAsserts();
        if (assertions == null) {
            throw new PerfanaClientException("Perfana assertions could not be checked, received null");
        }

        Configuration config = Configuration.defaultConfiguration()
                .addOptions(Option.SUPPRESS_EXCEPTIONS);

        ParseContext jsonPath = JsonPath.using(config);

        Boolean benchmarkBaselineTestRunResult = jsonPath.parse(assertions).read("$.benchmarkBaselineTestRun.result");
        String benchmarkBaselineTestRunDeeplink = jsonPath.parse(assertions).read("$.benchmarkBaselineTestRun.deeplink");
        Boolean benchmarkPreviousTestRunResult = jsonPath.parse(assertions).read("$.benchmarkPreviousTestRun.result");
        String benchmarkPreviousTestRunDeeplink = jsonPath.parse(assertions).read("$.benchmarkPreviousTestRun.deeplink");
        Boolean requirementsResult = jsonPath.parse(assertions).read("$.requirements.result");
        String requirementsDeeplink = jsonPath.parse(assertions).read("$.requirements.deeplink");

        logger.info(String.format("benchmarkBaselineTestRunResult: %s", benchmarkBaselineTestRunResult));
        logger.info(String.format("benchmarkPreviousTestRunResult: %s", benchmarkPreviousTestRunResult));
        logger.info(String.format("requirementsResult: %s", requirementsResult));

        StringBuilder assertionText = new StringBuilder();
        if (assertions.contains("false")) {

            assertionText.append("One or more Perfana assertions are failing: \n");
            if(requirementsResult != null && !requirementsResult) assertionText.append(String.format("Requirements failed: %s\n", requirementsDeeplink)) ;
            if(benchmarkPreviousTestRunResult != null && !benchmarkPreviousTestRunResult) assertionText.append(String.format("Benchmark to previous test run failed: %s\n", benchmarkPreviousTestRunDeeplink));
            if(benchmarkBaselineTestRunResult != null && !benchmarkBaselineTestRunResult) assertionText.append(String.format("Benchmark to baseline test run failed: %s", benchmarkBaselineTestRunDeeplink));

            logger.info(String.format("assertionText: %s", assertionText));

            throw new PerfanaClientException(assertionText.toString());
        }
        else {

            assertionText.append("All Perfana assertions are OK: \n");
            if(requirementsResult) assertionText.append(requirementsDeeplink).append("\n");
            if(benchmarkPreviousTestRunResult != null && benchmarkPreviousTestRunResult) assertionText.append(benchmarkPreviousTestRunDeeplink).append("\n");
            if(benchmarkBaselineTestRunResult != null && benchmarkBaselineTestRunResult) assertionText.append(benchmarkBaselineTestRunDeeplink);

            logger.info(String.format("assertionText: %s", assertionText));
        }
        return assertionText.toString();
    }
    
}