/*
 *    Copyright 2020-2023  Peter Paul Bakker @ perfana.io, Daniel Moll @ perfana.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");

    private String annotations = "";
    private String systemUnderTest = "unknown";
    private String workload = "unknown";
    private String testEnvironment = "unknown";
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

    public PerfanaTestContext build() {
        return new PerfanaTestContext(systemUnderTest, workload, testEnvironment, testRunId, version, ciBuildResultsUrl, rampupTime, constantLoadTime, annotations, variables, tags);
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