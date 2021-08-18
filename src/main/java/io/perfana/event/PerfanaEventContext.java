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

import nl.stokpop.eventscheduler.api.config.EventContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PerfanaEventContext extends EventContext {

    private final String perfanaUrl;
    private final String apiKey;
    private final boolean assertResultsEnabled;
    private final Map<String,String> variables;

    protected PerfanaEventContext(EventContext context, String perfanaUrl, String apiKey, boolean assertResultsEnabled, Map<String, String> variables) {
        super(context, PerfanaEventFactory.class.getName(), false);
        this.perfanaUrl = perfanaUrl;
        this.apiKey = apiKey;
        this.assertResultsEnabled = assertResultsEnabled;
        this.variables = Collections.unmodifiableMap(new HashMap<>(variables));
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

    @Override
    public String toString() {
        return "PerfanaEventConfig{" +
            "perfanaUrl='" + perfanaUrl + '\'' +
            ", apiKey=" + (apiKey == null ? "[not set]" : "[set]") +
            ", assertResultsEnabled=" + assertResultsEnabled +
            ", variables=" + variables +
            "} " + super.toString();
    }
}
