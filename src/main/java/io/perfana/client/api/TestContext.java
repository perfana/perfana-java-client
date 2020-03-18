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

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class TestContext {

    private final String systemUnderTest;
    private final String workload;
    private final String testEnvironment;
    private final String testRunId;
    private final String version;
    private final String CIBuildResultsUrl;
    private final Duration rampupTime;
    private final Duration plannedDuration;
    private final String annotations;
    private final Map<String, String> variables;
    private final List<String> tags;

    TestContext(String systemUnderTest, String workload, String testEnvironment, String testRunId, String CIBuildResultsUrl, String version, Duration rampupTime, Duration plannedDuration, String annotations, Map<String, String> variables, List<String> tags) {
        this.systemUnderTest = systemUnderTest;
        this.workload = workload;
        this.testEnvironment = testEnvironment;
        this.testRunId = testRunId;
        this.CIBuildResultsUrl = CIBuildResultsUrl;
        this.version = version;
        this.rampupTime = rampupTime;
        this.plannedDuration = plannedDuration;
        this.annotations = annotations;
        this.variables = variables;
        this.tags = tags;
    }

    public String getSystemUnderTest() {
        return systemUnderTest;
    }

    public String getWorkload() {
        return workload;
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

    public String getVersion() {
        return version;
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
