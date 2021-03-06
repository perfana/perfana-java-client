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
package io.perfana.event;

import nl.stokpop.eventscheduler.api.config.EventConfig;
import nl.stokpop.eventscheduler.api.config.EventContext;
import nl.stokpop.eventscheduler.api.config.TestContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class PerfanaEventConfig extends EventConfig {

    private String perfanaUrl = "http://localhost:8888";
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

    @NotNull
    private PerfanaEventContext createPerfanaEventContext(EventContext context) {
        return new PerfanaEventContext(context, perfanaUrl, assertResultsEnabled, variables);
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
            "} " + super.toString();
    }
}
