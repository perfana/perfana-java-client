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
package io.perfana.client.api;

import io.perfana.client.PerfanaUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class TestContextBuilder {
    private static final int DEFAULT_RAMPUP_TIME_SECONDS = 0;
    private static final int DEFAULT_CONSTANT_LOAD_TIME_SECONDS = 600;

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH-mm-ss");

    private String annotations = "";
    private String application = "unknown";
    private String testType = "unknown";
    private String testEnvironment = "unknown";
    private String testRunId = "unknown_" + dateTimeFormatter.format(LocalDateTime.now());
    private String ciBuildResultsUrl = "unknown";
    private String applicationRelease = "unknown";
    private Duration rampupTime = Duration.ofSeconds(DEFAULT_RAMPUP_TIME_SECONDS);
    private Duration constantLoadTime = Duration.ofSeconds(DEFAULT_CONSTANT_LOAD_TIME_SECONDS);
    private Map<String, String> variables = Collections.emptyMap();
    private List<String> tags = Collections.emptyList();

    public TestContextBuilder setApplication(String application) {
        if (PerfanaUtils.hasValue(application)) {
            this.application = application;
        }
        return this;
    }

    public TestContextBuilder setTestType(String testType) {
        if (PerfanaUtils.hasValue(testType)) {
            this.testType = testType;
        }
        return this;
    }

    public TestContextBuilder setTestEnvironment(String testEnvironment) {
        if (PerfanaUtils.hasValue(testEnvironment)) {
            this.testEnvironment = testEnvironment;
        }
        return this;
    }

    public TestContextBuilder setTestRunId(String testRunId) {
        if (PerfanaUtils.hasValue(testRunId)) {
            this.testRunId = testRunId;
        }
        return this;
    }

    public TestContextBuilder setCIBuildResultsUrl(String ciBuildResultsUrl) {
        if (PerfanaUtils.hasValue(ciBuildResultsUrl)) {
            this.ciBuildResultsUrl = ciBuildResultsUrl;
        }
        return this;
    }

    public TestContextBuilder setApplicationRelease(String applicationRelease) {
        if (PerfanaUtils.hasValue(applicationRelease)) {
            this.applicationRelease = applicationRelease;
        }
        return this;
    }

    public TestContextBuilder setRampupTime(Duration rampupTime) {
        if (rampupTime != null) {
            this.rampupTime = rampupTime;
        }
        return this;
    }

    public TestContextBuilder setConstantLoadTime(Duration constantLoadTime) {
        if (constantLoadTime != null) {
            this.constantLoadTime = constantLoadTime;
        }
        return this;
    }

    public TestContextBuilder setAnnotations(String annotations) {
        if (PerfanaUtils.hasValue(annotations)) {
            this.annotations = annotations;
        }
        return this;
    }

    public TestContextBuilder setVariables(Map<String, String> variables) {
        if (variables != null) {
            this.variables = variables;
        }
        return this;
    }

    public TestContextBuilder setTags(List<String> tags) {
        if (tags != null) {
            this.tags = tags;
        }
        return this;
    }

    /**
     * A comma separated list of tags.
     */
    public TestContextBuilder setTags(String tagsSeparatedByCommas) {
        this.tags = PerfanaUtils.splitAndTrim(tagsSeparatedByCommas,",");
        return this;
    }

    public TestContext build() {
        return new TestContext(application, testType, testEnvironment, testRunId, ciBuildResultsUrl, applicationRelease, rampupTime, constantLoadTime, annotations, variables, tags);
    }

    public TestContextBuilder setRampupTimeInSeconds(String rampupTimeSeconds) {
        this.rampupTime = Duration.ofSeconds(PerfanaUtils.parseInt("rampupTimeSeconds", rampupTimeSeconds, DEFAULT_RAMPUP_TIME_SECONDS));
        return this;
    }

    public TestContextBuilder setConstantLoadTimeInSeconds(String constantLoadTimeInSeconds) {
        this.constantLoadTime = Duration.ofSeconds(PerfanaUtils.parseInt("constantLoadTimeInSeconds", constantLoadTimeInSeconds, DEFAULT_CONSTANT_LOAD_TIME_SECONDS));
        return this;
    }

    public TestContextBuilder setVariables(Properties props) {
        if (props != null) {
            Map<String, String> vars = props.entrySet().stream().collect(
                    Collectors.toMap(
                            e -> e.getKey().toString(),
                            e -> e.getValue().toString()
                    )
            );
            this.setVariables(vars);
        }
        return this;
    }


}