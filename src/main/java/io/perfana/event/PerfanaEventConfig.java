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

import io.perfana.client.api.PerfanaConnectionSettingsBuilder;
import io.perfana.eventscheduler.api.config.EventConfig;
import io.perfana.eventscheduler.api.config.EventContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class PerfanaEventConfig extends EventConfig {

    private String perfanaUrl = "http://localhost:8888";
    private String apiKey = null;
    private boolean assertResultsEnabled = false;
    private Map<String,String> variables = Collections.emptyMap();

    private int retryCount = PerfanaConnectionSettingsBuilder.DEFAULT_RETRY_MAX_COUNT;

    private int retryDelaySeconds = PerfanaConnectionSettingsBuilder.DEFAULT_RETRY_TIME_SECONDS;

    private boolean overrideTestRunId = true;

    public void setPerfanaUrl(String perfanaUrl) {
        this.perfanaUrl = perfanaUrl;
    }

    public void setAssertResultsEnabled(boolean assertResultsEnabled) {
        this.assertResultsEnabled = assertResultsEnabled;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setRetryDelaySeconds(int retryDelaySeconds) {
        this.retryDelaySeconds = retryDelaySeconds;
    }

    @NotNull
    private PerfanaEventContext createPerfanaEventContext(EventContext context) {
        return new PerfanaEventContext(context, perfanaUrl, apiKey,
                assertResultsEnabled, variables, retryCount,
                retryDelaySeconds, overrideTestRunId);
    }

    public void setOverrideTestRunId(boolean overrideTestRunId) {
        this.overrideTestRunId = overrideTestRunId;
    }

    @Override
    public PerfanaEventContext toContext() {
        return createPerfanaEventContext(super.toContext());
    }

    @Override
    public String toString() {
        return "PerfanaEventConfig{" +
                "perfanaUrl='" + perfanaUrl + '\'' +
                ", apiKey=" + (apiKey == null ? "[not set]" : "[set]") +
                ", assertResultsEnabled=" + assertResultsEnabled +
                ", variables=" + variables +
                ", retryCount=" + retryCount +
                ", retryDelaySeconds=" + retryDelaySeconds +
                '}' + super.toString();
    }
}
