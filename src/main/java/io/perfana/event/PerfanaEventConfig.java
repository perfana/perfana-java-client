/*
 *    Copyright 2020-2021  Peter Paul Bakker @ Stokpop, Daniel Moll @ Perfana.io
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

import nl.stokpop.eventscheduler.api.config.EventConfig;
import nl.stokpop.eventscheduler.api.config.EventContext;
import nl.stokpop.eventscheduler.api.config.TestContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class PerfanaEventConfig extends EventConfig {

    private String perfanaUrl = "http://localhost:8888";
    private String apiKey = null;
    private boolean assertResultsEnabled = false;
    private Map<String,String> variables = Collections.emptyMap();

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

    @NotNull
    private PerfanaEventContext createPerfanaEventContext(EventContext context) {
        return new PerfanaEventContext(context, perfanaUrl, apiKey, assertResultsEnabled, variables);
    }

    @Override
    public PerfanaEventContext toContext() {
        return createPerfanaEventContext(super.toContext());
    }

    @Override
    public EventContext toContext(TestContext overrideTestContext) {
        return createPerfanaEventContext(super.toContext(overrideTestContext));
    }

    @Override
    public String toString() {
        return "PerfanaEventConfig{" +
            "perfanaUrl='" + perfanaUrl + '\'' +
            ", assertResultsEnabled=" + assertResultsEnabled +
            ", variables=" + variables +
            ", apiKey=" + (apiKey == null ? "[not set]" : "[set]") +
            "} " + super.toString();
    }
}
