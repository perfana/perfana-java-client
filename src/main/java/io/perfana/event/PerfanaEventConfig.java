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

import java.util.Collections;
import java.util.Map;

public class PerfanaEventConfig extends EventConfig {

    private String perfanaUrl = "http://localhost:8888";
    private boolean assertResultsEnabled = false;
    private Map<String,String> variables = Collections.emptyMap();

    @Override
    public String getEventFactory() {
        return PerfanaEventFactory.class.getName();
    }

    public String getPerfanaUrl() {
        return perfanaUrl;
    }

    public void setPerfanaUrl(String perfanaUrl) {
        this.perfanaUrl = perfanaUrl;
    }

    public boolean isAssertResultsEnabled() {
        return assertResultsEnabled;
    }

    public void setAssertResultsEnabled(boolean assertResultsEnabled) {
        this.assertResultsEnabled = assertResultsEnabled;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
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
