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
package io.perfana.event;

import io.perfana.eventscheduler.api.config.EventContext;

import java.util.Map;

public class PerfanaEventContext extends EventContext {

    private final String perfanaUrl;
    private final String apiKey;
    private final boolean assertResultsEnabled;
    private final Map<String,String> variables;

    private final boolean overrideTestRunId;

    private final int retryCount;

    private final int retryDelaySeconds;

    protected PerfanaEventContext(EventContext context, String perfanaUrl, String apiKey, boolean assertResultsEnabled, Map<String, String> variables, int retryCount, int retryDelaySeconds, boolean overrideTestRunId) {
        super(context, PerfanaEventFactory.class.getName(), false);
        this.perfanaUrl = perfanaUrl;
        this.apiKey = apiKey;
        this.assertResultsEnabled = assertResultsEnabled;
        this.variables = Map.copyOf(variables);
        this.retryCount = retryCount;
        this.retryDelaySeconds = retryDelaySeconds;
        this.overrideTestRunId = overrideTestRunId;
    }

    public String getPerfanaUrl() {
        return perfanaUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isAssertResultsEnabled() {
        return assertResultsEnabled;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getRetryDelaySeconds() {
        return retryDelaySeconds;
    }

    public boolean isOverrideTestRunId() {
        return overrideTestRunId;
    }

    @Override
    public String toString() {
        return "PerfanaEventContext{" +
                "perfanaUrl='" + perfanaUrl + '\'' +
                ", apiKey=" + (apiKey == null ? "[not set]" : "[set]") +
                ", assertResultsEnabled=" + assertResultsEnabled +
                ", variables=" + variables +
                ", retryCount=" + retryCount +
                ", retryDelaySeconds=" + retryDelaySeconds +
                '}' + super.toString();
    }
}
