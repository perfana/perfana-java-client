package io.perfana.client.api;

import io.perfana.client.PerfanaUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

public class PerfanaTestContextBuilder {
    private static final int DEFAULT_RAMPUP_TIME_SECONDS = 0;
    private static final int DEFAULT_CONSTANT_LOAD_TIME_SECONDS = 600;

    private String annotations = "";
    private String application = "unknown";
    private String testType = "unknown";
    private String testEnvironment = "unknown";
    private String testRunId = "unknown_" + System.currentTimeMillis();
    private String ciBuildResultsUrl = "unknown";
    private String applicationRelease = "unknown";
    private Duration rampupTime = Duration.ofSeconds(DEFAULT_RAMPUP_TIME_SECONDS);
    private Duration constantLoadTime = Duration.ofSeconds(DEFAULT_CONSTANT_LOAD_TIME_SECONDS);
    private Map<String, String> variables = Collections.emptyMap();

    public PerfanaTestContextBuilder setApplication(String application) {
        if (PerfanaUtils.hasValue(application)) {
            this.application = application;
        }
        return this;
    }

    public PerfanaTestContextBuilder setTestType(String testType) {
        if (PerfanaUtils.hasValue(testType)) {
            this.testType = testType;
        }
        return this;
    }

    public PerfanaTestContextBuilder setTestEnvironment(String testEnvironment) {
        if (PerfanaUtils.hasValue(testEnvironment)) {
            this.testEnvironment = testEnvironment;
        }
        return this;
    }

    public PerfanaTestContextBuilder setTestRunId(String testRunId) {
        if (PerfanaUtils.hasValue(testRunId)) {
            this.testRunId = testRunId;
        }
        return this;
    }

    public PerfanaTestContextBuilder setCIBuildResultsUrl(String ciBuildResultsUrl) {
        if (PerfanaUtils.hasValue(ciBuildResultsUrl)) {
            this.ciBuildResultsUrl = ciBuildResultsUrl;
        }
        return this;
    }

    public PerfanaTestContextBuilder setApplicationRelease(String applicationRelease) {
        if (PerfanaUtils.hasValue(applicationRelease)) {
            this.applicationRelease = applicationRelease;
        }
        return this;
    }

    public PerfanaTestContextBuilder setRampupTime(Duration rampupTime) {
        if (rampupTime != null) {
            this.rampupTime = rampupTime;
        }
        return this;
    }

    public PerfanaTestContextBuilder setConstantLoadTime(Duration constantLoadTime) {
        if (constantLoadTime != null) {
            this.constantLoadTime = constantLoadTime;
        }
        return this;
    }

    public PerfanaTestContextBuilder setAnnotations(String annotations) {
        if (PerfanaUtils.hasValue(annotations)) {
            this.annotations = annotations;
        }
        return this;
    }

    public PerfanaTestContextBuilder setVariables(Map<String, String> variables) {
        if (variables != null) {
            this.variables = variables;
        }
        return this;
    }

    public PerfanaTestContext build() {
        return new PerfanaTestContext(application, testType, testEnvironment, testRunId, ciBuildResultsUrl, applicationRelease, rampupTime, constantLoadTime, annotations, variables);
    }

    public PerfanaTestContextBuilder setRampupTimeInSeconds(String rampupTimeSeconds) {
        this.rampupTime = Duration.ofSeconds(PerfanaUtils.parseInt("rampupTimeSeconds", rampupTimeSeconds, DEFAULT_RAMPUP_TIME_SECONDS));
        return this;
    }

    public PerfanaTestContextBuilder setConstantLoadTimeInSeconds(String constantLoadTimeInSeconds) {
        this.rampupTime = Duration.ofSeconds(PerfanaUtils.parseInt("constantLoadTimeInSeconds", constantLoadTimeInSeconds, DEFAULT_CONSTANT_LOAD_TIME_SECONDS));
        return this;
    }
}