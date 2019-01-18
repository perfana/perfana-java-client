package io.perfana.client;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
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
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class PerfanaClient {

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    private Logger logger;

    private final String application;
    private final String testType;
    private final String testEnvironment;
    private final String testRunId;
    private final String CIBuildResultsUrl;
    private final String applicationRelease;
    private final String perfanaUrl;
    private final Duration rampupTime;
    private final Duration plannedDuration;
    private final String annotations;
    private final Map<String, String> variables;
    private final boolean assertResultsEnabled;
    private final int retryMaxCount;
    private final Duration retryDuration;
    private final Duration keepAliveDuration;

    private final PerfanaEventBroadcaster broadcaster;
    private final PerfanaEventProperties eventProperties;
    private final List<ScheduleEvent> scheduleEvents;

    private ScheduledExecutorService executorKeepAlive;
    private ScheduledExecutorService executorCustomEvents;

    PerfanaClient(String application, String testType, String testEnvironment, String testRunId,
                  String CIBuildResultsUrl, String applicationRelease, Duration rampupTime,
                  Duration constantLoadTime, String perfanaUrl, String annotations,
                  Map<String, String> variables, boolean assertResultsEnabled, PerfanaEventBroadcaster broadcaster,
                  PerfanaEventProperties eventProperties,
                  int retryMaxCount, Duration retryDuration, Duration keepAliveDuration,
                  List<ScheduleEvent> scheduleEvents) {
        this.application = application;
        this.testType = testType;
        this.testEnvironment = testEnvironment;
        this.testRunId = testRunId;
        this.CIBuildResultsUrl = CIBuildResultsUrl;
        this.applicationRelease = applicationRelease;
        this.rampupTime = rampupTime;
        this.plannedDuration = rampupTime.plus(constantLoadTime);
        this.perfanaUrl = perfanaUrl;
        this.annotations = annotations;
        this.variables = variables;
        this.assertResultsEnabled = assertResultsEnabled;
        this.broadcaster = broadcaster;
        this.eventProperties = eventProperties;
        this.retryMaxCount = retryMaxCount;
        this.retryDuration = retryDuration;
        this.keepAliveDuration = keepAliveDuration;
        this.scheduleEvents = scheduleEvents;
    }

    public void startSession() {
        logger.info("Perfana start session");

        logger.info("Perfana broadcast event before test");
        broadcaster.broadcastBeforeTest(testRunId, eventProperties);

        if (executorKeepAlive != null) {
            throw new RuntimeException("Cannot start perfana session multiple times!");
        }

        logger.info(String.format("Calling Perfana (%s) keep alive every %d seconds.", perfanaUrl, keepAliveDuration.getSeconds()));

        executorKeepAlive = createKeepAliveScheduler();
        executorKeepAlive.scheduleAtFixedRate(new KeepAliveRunner(this), 0, keepAliveDuration.getSeconds(), TimeUnit.SECONDS);

        callPerfanaEvent("Test start");
        startCustomEventScheduler(scheduleEvents);
    }

    private ScheduledExecutorService createKeepAliveScheduler() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            String threadName = "Perfana-Keep-Alive-Thread";
            logger.info("Creating new thead: " + threadName);
            return new Thread(r, threadName);
        });
    }

    private void startCustomEventScheduler(final List<ScheduleEvent> scheduleEvents) {
        if (!(scheduleEvents == null || scheduleEvents.isEmpty())) {

            logger.info(createEventScheduleMessage(scheduleEvents));

            executorCustomEvents = createCustomEventScheduler();
            scheduleEvents.forEach(event -> addToExecutor(executorCustomEvents, event));
        }
        else {
            logger.info("No custom Perfana schedule events found.");
        }
    }

    private ScheduledExecutorService createCustomEventScheduler() {
        return Executors.newScheduledThreadPool(4, new ThreadFactory() {
            private final AtomicInteger perfanaThreadCount = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                String threadName = "Perfana-Custom-Event-Thread-" + perfanaThreadCount.incrementAndGet();
                logger.info("Creating new thead: " + threadName);
                return new Thread(r, threadName);
            }
        });
    }

    private String createEventScheduleMessage(List<ScheduleEvent> scheduleEvents) {
        StringBuilder message = new StringBuilder();
        message.append("=== custom Perfana events schedule ===");
        scheduleEvents.forEach(event -> message.append("\n==> ").append(event));
        return message.toString();
    }

    private void addToExecutor(final ScheduledExecutorService executorService, final ScheduleEvent event) {
        executorService.schedule(new EventRunner(testRunId, eventProperties, event, broadcaster, this), event.getDuration().getSeconds(), TimeUnit.SECONDS);
    }

    public void stopSession() throws PerfanaClientException {
        logger.info("Perfana end session.");
        shutdownThreadsNow();

        logger.info("Perfana broadcast event after test");
        callPerfanaEvent("Test finish");
        broadcaster.broadcastAfterTest(testRunId, eventProperties);

        callPerfana(true);
        assertResults();
    }

    /**
     * Call to abort this test run.
     * 
     * @throw PerfanaClientException
     */
    public void abortSession() throws PerfanaClientException {
        logger.warn("Perfana session abort called.");
        shutdownThreadsNow();

        logger.info("Perfana broadcast event after test");
        callPerfanaEvent("Test aborted");
        broadcaster.broadcastAfterTest(testRunId, eventProperties);

        assertResults();
    }

    private void shutdownThreadsNow() {
        if (executorKeepAlive != null) {
            executorKeepAlive.shutdownNow();
        }
        if (executorCustomEvents != null) {
            List<Runnable> runnables = executorCustomEvents.shutdownNow();
            if (runnables.size() > 0) {
                logger.warn("There are " + runnables.size() + " custom Perfana events that are not (fully) executed!");
            }
        }
        executorKeepAlive = null;
        executorCustomEvents = null;
    }

    void injectLogger(Logger logger) {
        this.logger = logger;
    }

    private void callPerfana(boolean completed) {
        String json = perfanaMessageToJson(application, testType, testEnvironment, testRunId, CIBuildResultsUrl, applicationRelease, rampupTime, plannedDuration, annotations, variables, completed);
        String testUrl = perfanaUrl + "/test";
        logger.debug(String.format("Call to endpoint: %s with json: %s", testUrl, json));
        try {
            String result = post(testUrl, json);
            logger.debug("Result: " + result);
        } catch (IOException e) {
            logger.error("Failed to call perfana: " + e.getMessage());
        }
    }

    private void callPerfanaEvent(String eventDescription) {
        String json = perfanaEventToJson(application, testType, testEnvironment, testRunId, eventDescription);
        String eventsUrl = perfanaUrl + "/events";
        logger.debug(String.format("Add perfana event to endpoint: %s with json: %s", eventsUrl, json));
        try {
            String result = post(eventsUrl, json);
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
            if (!response.isSuccessful()) {
                logger.warn("Post was not successful: " + response);
            }
            return responseBody == null ? "null" : responseBody.string();
        }
    }

    private String perfanaMessageToJson(String application, String testType, String testEnvironment, String testRunId, String CIBuildResultsUrl, String applicationRelease, Duration rampupTime, Duration plannedDuration, String annotations, Map<String, String> variables, boolean completed) {

        JSONObject json = new JSONObject();

        /* If variables parameter exists add them to the json */
        if(variables != null && !variables.isEmpty()) {
            JSONArray variablesArray = new JSONArray();
            variables.forEach((key, value) -> variablesArray.add(createVariables(key, value)));
            json.put("variables", variablesArray);
        }

        /* If annotations are passed add them to the json */
        if(annotations != null && annotations.isEmpty()){
            json.put("annotations", annotations);
        }

        json.put("testRunId", testRunId);
        json.put("testType", testType);
        json.put("testEnvironment", testEnvironment);
        json.put("application", application);
        json.put("applicationRelease", applicationRelease);
        json.put("CIBuildResultsUrl", CIBuildResultsUrl);
        json.put("rampUp", String.valueOf(rampupTime.getSeconds()));
        json.put("duration", String.valueOf(plannedDuration.getSeconds()));
        json.put("completed", completed);

        return json.toJSONString();
    }

    private static JSONObject createVariables(String name, String value) {
        JSONObject variables = new JSONObject();
        variables.put("placeholder", name);
        variables.put("value", value);
        return variables;
    }

    private String perfanaEventToJson(String application, String testType, String testEnvironment, String testRunId, String eventDescription) {
        JSONObject json = new JSONObject();

        json.put("application", application);
        json.put("testEnvironment", testEnvironment);
        json.put("title", testRunId);
        json.put("description", eventDescription);

        JSONArray tags = new JSONArray();
        tags.add(testType);
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
            url = String.join("/", perfanaUrl, "get-benchmark-results", URLEncoder.encode(application, "UTF-8").replaceAll("\\+", "%20"), URLEncoder.encode(testRunId, "UTF-8").replaceAll("\\+", "%20") );
        } catch (UnsupportedEncodingException e) {
            throw new PerfanaClientException("Cannot encode perfana url.", e);
        }
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        int retryCount = 0;
        String assertions = null;

        boolean assertionsAvailable = false;
        while (retryCount++ < retryMaxCount) {
            try {
                Thread.sleep(retryDuration.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // ignore
            }
            try (Response response = client.newCall(request).execute()) {
                ResponseBody responseBody = response.body();
                if (response.code() == 200) {
                    assertions = (responseBody == null) ? "null" : responseBody.string();
                    assertionsAvailable = true;
                    break;
                } else {
                    String message = (responseBody == null) ? response.message() : responseBody.string();
                    logger.info(
                            String.format("failed to retrieve assertions for url [%s] code [%d] retry [%d/%d] %s",
                            url, response.code(), retryCount, retryMaxCount, message));
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

    private String assertResults() throws PerfanaClientException {

        if (!assertResultsEnabled) {
            String message = "Perfana assert results is not enabled.";
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
        DocumentContext documentContext = parseContext.parse(assertions);

        Boolean benchmarkBaselineTestRunResult = documentContext.read("$.benchmarkBaselineTestRun.result");
        String benchmarkBaselineTestRunDeeplink = documentContext.read("$.benchmarkBaselineTestRun.deeplink");
        Boolean benchmarkPreviousTestRunResult = documentContext.read("$.benchmarkPreviousTestRun.result");
        String benchmarkPreviousTestRunDeeplink = documentContext.read("$.benchmarkPreviousTestRun.deeplink");
        Boolean requirementsResult = documentContext.read("$.requirements.result");
        String requirementsDeeplink = documentContext.read("$.requirements.deeplink");

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

            throw new PerfanaClientException(text.toString());
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

            logger.info(String.format("The assertionText: %s", text));
        }
        return text.toString();
    }

    private static boolean hasSucceeded(Boolean testResult) {
        return testResult != null && testResult;
    }

    private static boolean hasFailed(Boolean testResult) {
        return testResult != null && !testResult;
    }

    public interface Logger {
        void info(String message);
        void warn(String message);
        void error(String message);
        void debug(String message);
    }

    public static class KeepAliveRunner implements Runnable {

        private final PerfanaClient client;

        KeepAliveRunner(PerfanaClient client) {
            this.client = client;
        }

        @Override
        public void run() {
            client.callPerfana(false);
            client.broadcaster.broadCastKeepAlive(client.testRunId, client.eventProperties);
        }

        @Override
        public String toString() {
            return "KeepAliveRunner for " + client;
        }
    }

    public static class EventRunner implements Runnable {
        private final String testId;
        private final PerfanaEventProperties eventProperties;

        private final ScheduleEvent event;

        private final PerfanaEventBroadcaster eventBroadcaster;
        private final PerfanaClient client;

        public EventRunner(String testId, PerfanaEventProperties eventProperties, ScheduleEvent event, PerfanaEventBroadcaster eventBroadcaster, PerfanaClient perfanaClient) {
            this.testId = testId;
            this.eventProperties = eventProperties;
            this.event = event;
            this.eventBroadcaster = eventBroadcaster;
            this.client = perfanaClient;
        }

        @Override
        public void run() {
            client.logger.info("Fire Perfana custom event [" + event + "]");
            client.callPerfanaEvent(event.getName());
            eventBroadcaster.broadcastCustomEvent(testId, eventProperties, event);
        }

        @Override
        public String toString() {
            return String.format("EventRunner for event %s for testId %s", event, testId);
        }
    }

    @Override
    public String toString() {
        return String.format("PerfanaClient [testRunId:%s testType:%s testEnv:%s perfanaUrl:%s]",
                this.testRunId, this.testType, this.testEnvironment, this.perfanaUrl);
    }
}