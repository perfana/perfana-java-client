package io.perfana.client;

import io.perfana.service.PerfanaEventBroadcaster;
import io.perfana.service.PerfanaEventProvider;

import java.util.Properties;

public class PerfanaClientBuilder {

    private String application = "unknown";
    private String testType = "unknown";
    private String testEnvironment = "unknown";
    private String testRunId = "unknown";
    private String ciBuildResultsUrl = "unknown";
    private String applicationRelease = "unknown";
    private String rampupTimeInSeconds = "0";
    private String constantLoadTimeInSeconds = "0";
    private String perfanaUrl = "unknown";
    private String annotations = "";
    private Properties variables = new Properties();
    private boolean assertResultsEnabled = false;
    private PerfanaEventBroadcaster broadcaster;

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
            System.out.printf("[%s]` %s%n", level, something);
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
        this.rampupTimeInSeconds = rampupTimeInSeconds;
        return this;
    }

    public PerfanaClientBuilder setConstantLoadTimeInSeconds(final String constantLoadTimeInSeconds) {
        this.constantLoadTimeInSeconds = constantLoadTimeInSeconds;
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

    public PerfanaClient createPerfanaClient() {

        // get default broadcaster
        PerfanaEventBroadcaster broadcaster = this.broadcaster == null ? PerfanaEventProvider.getInstance() : this.broadcaster;

        PerfanaClient perfanaClient = new PerfanaClient(application, testType, testEnvironment, testRunId, ciBuildResultsUrl, applicationRelease, rampupTimeInSeconds, constantLoadTimeInSeconds, perfanaUrl, annotations, variables, assertResultsEnabled, broadcaster);
        perfanaClient.injectLogger(logger);
        return perfanaClient;
    }

}