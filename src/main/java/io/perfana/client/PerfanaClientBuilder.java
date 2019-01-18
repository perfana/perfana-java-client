package io.perfana.client;

import io.perfana.event.PerfanaEventBroadcaster;
import io.perfana.event.PerfanaEventProperties;
import io.perfana.event.PerfanaEventProvider;
import io.perfana.event.ScheduleEvent;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class PerfanaClientBuilder {

    private static final int DEFAULT_RAMPUP_TIME_SECONDS = 0;
    private static final int DEFAULT_CONSTANT_LOAD_TIME_SECONDS = 600;
    private static final int DEFAULT_KEEP_ALIVE_TIME_SECONDS = 30;
    private static final int DEFAULT_RETRY_TIME_SECONDS = 10;
    private static final int DEFAULT_RETRY_MAX_COUNT = 30;

    private String application = "unknown";
    private String testType = "unknown";
    private String testEnvironment = "unknown";
    private String testRunId = "unknown_" + System.currentTimeMillis();
    private String ciBuildResultsUrl = "unknown";
    private String applicationRelease = "unknown";
    private Duration rampupTime = Duration.ofSeconds(DEFAULT_RAMPUP_TIME_SECONDS);
    private Duration constantLoadTime = Duration.ofSeconds(DEFAULT_CONSTANT_LOAD_TIME_SECONDS);
    private String perfanaUrl = "unknown";
    private String annotations = "";
    private Duration keepAliveTime = Duration.ofSeconds(DEFAULT_KEEP_ALIVE_TIME_SECONDS);
    private int retryMaxCount = DEFAULT_RETRY_MAX_COUNT;
    private Duration retryDuration = Duration.ofSeconds(DEFAULT_RETRY_TIME_SECONDS);
    private Map<String, String> variables = Collections.emptyMap();
    private boolean assertResultsEnabled = false;
    private PerfanaEventBroadcaster broadcaster;
    private PerfanaEventProperties eventProperties = new PerfanaEventProperties();
    private List<ScheduleEvent> scheduleEvents = Collections.emptyList();

    private PerfanaClient.Logger logger = new PerfanaClient.Logger() {
        @Override
        public void info(String message) {
            say("INFO ", message);
        }

        @Override
        public void warn(String message) {
            say("WARN ", message);
        }

        @Override
        public void error(String message) {
            say("ERROR", message);
        }

        @Override
        public void debug(String message) {
            say("DEBUG", message);
        }

        private void say(String level, String something) {
            System.out.printf("[%s] %s%n", level, something);
        }
    };

    public PerfanaClientBuilder setApplication(String application) {
        if (!isEmpty(application)) {
            this.application = application;
        }
        return this;
    }

    public PerfanaClientBuilder setTestType(String testType) {
        if (!isEmpty(testType)) {
            this.testType = testType;
        }
        return this;
    }

    public PerfanaClientBuilder setTestEnvironment(String testEnvironment) {
        if (!isEmpty(testEnvironment)) {
            this.testEnvironment = testEnvironment;
        }
        return this;
    }

    public PerfanaClientBuilder setTestRunId(String testRunId) {
        if (!isEmpty(testRunId)) {
            this.testRunId = testRunId;
        }
        return this;
    }

    public PerfanaClientBuilder setCIBuildResultsUrl(String ciBuildResultsUrl) {
        this.ciBuildResultsUrl = ciBuildResultsUrl;
        return this;
    }

    public PerfanaClientBuilder setApplicationRelease(String applicationRelease) {
        if (!isEmpty(applicationRelease)) {
            this.applicationRelease = applicationRelease;
        }
        return this;
    }

    public PerfanaClientBuilder setRampupTimeInSeconds(String rampupTimeInSeconds) {
        this.rampupTime = Duration.ofSeconds(parseInt("rampupTime", rampupTimeInSeconds, DEFAULT_RAMPUP_TIME_SECONDS));
        return this;
    }

    public PerfanaClientBuilder setConstantLoadTimeInSeconds(String constantLoadTimeInSeconds) {
        this.constantLoadTime = Duration.ofSeconds(parseInt("constantLoadTime", constantLoadTimeInSeconds, DEFAULT_CONSTANT_LOAD_TIME_SECONDS));
        return this;
    }

    public PerfanaClientBuilder setPerfanaUrl(String perfanaUrl) {
        this.perfanaUrl = perfanaUrl;
        return this;
    }

    public PerfanaClientBuilder setAnnotations(String annotations) {
        this.annotations = annotations;
        return this;
    }

    /**
     * @deprecated use setter with Map&lt;String,String&gt;
     */
    @Deprecated
    public PerfanaClientBuilder setVariables(Properties variables) {
        Map<String, String> keyValueMap = new HashMap<>(variables.size(), 1);
        variables.forEach((key,value) -> keyValueMap.put((String)key,(String)value));
        this.variables = keyValueMap;
        return this;
    }
    
    public PerfanaClientBuilder setVariables(Map<String, String> variables) {
        this.variables = variables;
        return this;
    }

    public PerfanaClientBuilder setAssertResultsEnabled(boolean assertResultsEnabled) {
        this.assertResultsEnabled = assertResultsEnabled;
        return this;
    }

    public PerfanaClientBuilder setLogger(PerfanaClient.Logger logger) {
        this.logger = logger;
        return this;
    }

    public PerfanaClientBuilder setBroadcaster(PerfanaEventBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
        return this;
    }

    public PerfanaClientBuilder setKeepAliveTimeInSeconds(String keepAliveTimeInSeconds) {
        this.keepAliveTime = Duration.ofSeconds(parseInt("keepAliveTimeInSeconds", keepAliveTimeInSeconds, DEFAULT_KEEP_ALIVE_TIME_SECONDS));
        return this;
    }

    public PerfanaClientBuilder setRetryMaxCount(String retryMaxCount) {
        this.retryMaxCount = parseInt("retryMaxCount", retryMaxCount, DEFAULT_RETRY_MAX_COUNT);
        return this;
    }

    public PerfanaClientBuilder setRetryTimeInSeconds(String retryTimeInSeconds) {
        this.retryDuration = Duration.ofSeconds(parseInt("retryTimeInSeconds", retryTimeInSeconds, DEFAULT_RETRY_TIME_SECONDS));
        return this;
    }
    
    private static boolean isEmpty(String variable) {
        return variable == null || variable.isEmpty();
    }

    /**
     * Add properties to be passed on to the event implementation class.
     * @param eventImplementationName the fully qualified implementation class name (class.getName())
     * @param name the name of the property (not null or empty), e.g. "REST_URL"
     * @param value the name of the property (can be null or empty), e.g. "https://my-rest-call"
     * @return this
     */
    public PerfanaClientBuilder addEventProperty(String eventImplementationName, String name, String value) {
        if (eventImplementationName == null || eventImplementationName.isEmpty()) {
            throw new PerfanaClientRuntimeException("EventImplementationName is null or empty for " + this);
        }
        if (name == null || name.isEmpty()) {
            throw new PerfanaClientRuntimeException("EventImplementation property name is null or empty for " + this);
        }
        eventProperties.put(eventImplementationName, name, value);
        return this;
    }
    
    public PerfanaClient createPerfanaClient() {

        // get default broadcaster if no broadcaster was given
        if (broadcaster == null) logger.info("Creating default Perfana event broadcaster.");
        PerfanaEventBroadcaster broadcaster = this.broadcaster == null ? PerfanaEventProvider.getInstance() : this.broadcaster;

        PerfanaClient perfanaClient = new PerfanaClient(
                application, testType, testEnvironment, testRunId,
                ciBuildResultsUrl, applicationRelease, rampupTime,
                constantLoadTime, perfanaUrl, annotations,
                variables, assertResultsEnabled, broadcaster, eventProperties,
                retryMaxCount, retryDuration, keepAliveTime, scheduleEvents);
        perfanaClient.injectLogger(logger);
        return perfanaClient;
    }

    private int parseInt(String variableName, String timeString) {
        return parseInt(variableName, timeString, 0);
    }
    
    private int parseInt(String variableName, String timeString, int defaultValue) {
        int time;
        try {
            time = Integer.parseInt(timeString);
        } catch (NumberFormatException e) {
            logger.error(String.format("Unable to parse value of [%s=%s]: using default value [%d]. Error message: %s.", variableName, timeString, defaultValue, e.getMessage()));
            time = defaultValue;
        }
        return time;
    }

    /**
     * Provide schedule event as "duration|eventname|json-settings(optional)".
     * The duration is in ISO-8601 format period format, e.g. 3 minutes 15 seconds
     * is P3M15S.
     *
     * One schedule event per line.
     */
    public PerfanaClientBuilder setScheduleEvents(String eventSchedule) {
        BufferedReader eventReader = new BufferedReader(new StringReader(eventSchedule));
        return setScheduleEvents(eventReader.lines()
                .map(String::trim)
                .filter(e -> !e.isEmpty())
                .collect(Collectors.toList()));
    }

    /**
     * Provide list of schedule events.
     * @see PerfanaClientBuilder#setScheduleEvents(String) 
     */
    public PerfanaClientBuilder setScheduleEvents(List<String> scheduleEvents) {
        this.scheduleEvents = parseScheduleEvents(scheduleEvents);
        return this;
    }

    private List<ScheduleEvent> parseScheduleEvents(List<String> eventSchedule) {
        return eventSchedule.stream()
                .map(ScheduleEvent::createFromLine)
                .collect(Collectors.toList());
    }
}