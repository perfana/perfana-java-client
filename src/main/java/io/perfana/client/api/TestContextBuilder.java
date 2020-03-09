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
    private String systemUnderTest = "unknown";
    private String workload = "unknown";
    private String environment = "unknown";
    private String testRunId = "unknown_" + dateTimeFormatter.format(LocalDateTime.now());
    private String ciBuildResultsUrl = "unknown";
    private String version = "unknown";
    private Duration rampupTime = Duration.ofSeconds(DEFAULT_RAMPUP_TIME_SECONDS);
    private Duration constantLoadTime = Duration.ofSeconds(DEFAULT_CONSTANT_LOAD_TIME_SECONDS);
    private Map<String, String> variables = Collections.emptyMap();
    private List<String> tags = Collections.emptyList();

    public TestContextBuilder setSystemUnderTest(String systemUnderTest) {
        if (PerfanaUtils.hasValue(systemUnderTest)) {
            this.systemUnderTest = systemUnderTest;
        }
        return this;
    }

    public TestContextBuilder setWorkload(String workload) {
        if (PerfanaUtils.hasValue(workload)) {
            this.workload = workload;
        }
        return this;
    }

    public TestContextBuilder setEnvironment(String environment) {
        if (PerfanaUtils.hasValue(environment)) {
            this.environment = environment;
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

    public TestContextBuilder setVersion(String version) {
        if (PerfanaUtils.hasValue(version)) {
            this.version = version;
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
        return new TestContext(systemUnderTest, workload, environment, testRunId, ciBuildResultsUrl, version, rampupTime, constantLoadTime, annotations, variables, tags);
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