package io.perfana.client;

import io.perfana.event.PerfanaEventBroadcaster;
import io.perfana.event.PerfanaEventProperties;
import io.perfana.event.PerfanaEventProvider;

import java.time.Duration;
import java.util.Properties;

public class PerfanaClientBuilder {

    private static final int DEFAULT_RAMPUP_TIME_SECONDS = 0;
    private static final int DEFAULT_CONSTANT_LOAD_TIME_SECONDS = 600;
    private static final int DEFAULT_KEEP_ALIVE_TIME_SECONDS = 30;
    private static final int DEFAULT_RETRY_TIME_SECONDS = 10;
    private static final int DEFAULT_RETRY_MAX_COUNT = 30;

    private String application = "unknown";
    private String testType = "unknown";
    private String testEnvironment = "unknown";
    private String testRunId = "unknown";
    private String ciBuildResultsUrl = "unknown";
    private String applicationRelease = "unknown";
    private Duration rampupTime = Duration.ofSeconds(DEFAULT_RAMPUP_TIME_SECONDS);
    private Duration constantLoadTime = Duration.ofSeconds(DEFAULT_CONSTANT_LOAD_TIME_SECONDS);
    private String perfanaUrl = "unknown";
    private String annotations = "";
    private Duration keepAliveTime = Duration.ofSeconds(DEFAULT_KEEP_ALIVE_TIME_SECONDS);
    private int retryMaxCount = DEFAULT_RETRY_MAX_COUNT;
    private Duration retryDuration = Duration.ofSeconds(DEFAULT_RETRY_TIME_SECONDS);
    private Properties variables = new Properties();
    private boolean assertResultsEnabled = false;
    private PerfanaEventBroadcaster broadcaster;
    private PerfanaEventProperties eventProperties = new PerfanaEventProperties();

    private PerfanaClient.Logger logger = new PerfanaClient.Logger() {
        @Override
        public void info(final String message) {
            say("INFO ", message);
        }

        @Override
        public void warn(final String message) {
            say("WARN ", message);
        }

        @Override
        public void error(final String message) {
            say("ERROR", message);
        }

        @Override
        public void debug(final String message) {
            say("DEBUG", message);
        }

        private void say(String level, String something) {
            System.out.printf("[%s] %s%n", level, something);
        }
    };

    public PerfanaClientBuilder setApplication(final String application) {
        this.application = application;
        return this;
    }

    public PerfanaClientBuilder setTestType(final String testType) {
        this.testType = testType;
        return this;
    }

    public PerfanaClientBuilder setTestEnvironment(final String testEnvironment) {
        this.testEnvironment = testEnvironment;
        return this;
    }

    public PerfanaClientBuilder setTestRunId(final String testRunId) {
        this.testRunId = testRunId;
        return this;
    }

    public PerfanaClientBuilder setCIBuildResultsUrl(final String ciBuildResultsUrl) {
        this.ciBuildResultsUrl = ciBuildResultsUrl;
        return this;
    }

    public PerfanaClientBuilder setApplicationRelease(final String applicationRelease) {
        this.applicationRelease = applicationRelease;
        return this;
    }

    public PerfanaClientBuilder setRampupTimeInSeconds(final String rampupTimeInSeconds) {
        this.rampupTime = Duration.ofSeconds(parseInt("rampupTime", rampupTimeInSeconds, DEFAULT_RAMPUP_TIME_SECONDS));
        return this;
    }

    public PerfanaClientBuilder setConstantLoadTimeInSeconds(final String constantLoadTimeInSeconds) {
        this.constantLoadTime = Duration.ofSeconds(parseInt("constantLoadTime", constantLoadTimeInSeconds, DEFAULT_CONSTANT_LOAD_TIME_SECONDS));
        return this;
    }

    public PerfanaClientBuilder setPerfanaUrl(final String perfanaUrl) {
        this.perfanaUrl = perfanaUrl;
        return this;
    }

    public PerfanaClientBuilder setAnnotations(final String annotations) {
        this.annotations = annotations;
        return this;
    }

    public PerfanaClientBuilder setVariables(final Properties variables) {
        this.variables = variables;
        return this;
    }

    public PerfanaClientBuilder setAssertResultsEnabled(final boolean assertResultsEnabled) {
        this.assertResultsEnabled = assertResultsEnabled;
        return this;
    }

    public PerfanaClientBuilder setLogger(final PerfanaClient.Logger logger) {
        this.logger = logger;
        return this;
    }

    public PerfanaClientBuilder setBroadcaster(final PerfanaEventBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
        return this;
    }

    public PerfanaClientBuilder setKeepAliveTimeInSeconds(final String keepAliveTimeInSeconds) {
        this.keepAliveTime = Duration.ofSeconds(parseInt("keepAliveTimeInSeconds", keepAliveTimeInSeconds, DEFAULT_KEEP_ALIVE_TIME_SECONDS));
        return this;
    }

    public PerfanaClientBuilder setRetryMaxCount(final String retryMaxCount) {
        this.retryMaxCount = parseInt("retryMaxCount", retryMaxCount, DEFAULT_RETRY_MAX_COUNT);
        return this;
    }

    public PerfanaClientBuilder setRetryTimeInSeconds(final String retryTimeInSeconds) {
        this.retryDuration = Duration.ofSeconds(parseInt("retryTimeInSeconds", retryTimeInSeconds, DEFAULT_RETRY_TIME_SECONDS));
        return this;
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
                retryMaxCount, retryDuration, keepAliveTime);
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
            logger.error(String.format("Unable to parse %s, will use %d instead: %s", variableName, defaultValue, e.getMessage()));
            time = defaultValue;
        }
        return time;
    }

}