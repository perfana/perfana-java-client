package io.perfana.client.api;

import java.time.Duration;
import java.util.Map;
import java.util.List;

public class TestContext {

    private final String application;
    private final String testType;
    private final String testEnvironment;
    private final String testRunId;
    private final String applicationRelease;
    private final String CIBuildResultsUrl;
    private final Duration rampupTime;
    private final Duration plannedDuration;
    private final String annotations;
    private final Map<String, String> variables;
    private final List<String> tags;

    TestContext(String application, String testType, String testEnvironment, String testRunId, String CIBuildResultsUrl, String applicationRelease, Duration rampupTime, Duration plannedDuration, String annotations, Map<String, String> variables, List<String> tags) {
        this.application = application;
        this.testType = testType;
        this.testEnvironment = testEnvironment;
        this.testRunId = testRunId;
        this.CIBuildResultsUrl = CIBuildResultsUrl;
        this.applicationRelease = applicationRelease;
        this.rampupTime = rampupTime;
        this.plannedDuration = plannedDuration;
        this.annotations = annotations;
        this.variables = variables;
        this.tags = tags;
    }

    public String getApplication() {
        return application;
    }

    public String getTestType() {
        return testType;
    }

    public String getTestEnvironment() {
        return testEnvironment;
    }

    public String getTestRunId() {
        return testRunId;
    }

    public String getCIBuildResultsUrl() {
        return CIBuildResultsUrl;
    }

    public String getApplicationRelease() {
        return applicationRelease;
    }
    
    public Duration getRampupTime() {
        return rampupTime;
    }

    public Duration getPlannedDuration() {
        return plannedDuration;
    }

    public String getAnnotations() {
        return annotations;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public List<String> getTags() { return tags; }

}
