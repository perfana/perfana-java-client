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
package io.perfana.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PerfanaTest {
    @JsonProperty("_id")
    private final String id;
    private final String systemUnderTest;
    private final String testRunId;
    private final String version;
    private final String environment;
    private final String workload;
    private final String CIBuildResultsUrl;

    private final int duration;
    private final int plannedDuration;
    private final int rampUp;

    private final String start; // 2020-02-21T20:59:31.206Z
    private final String end; // 2020-02-21T20:59:31.206Z
    private final String expires; // 2050-02-13T20:59:31.206Z

    private final boolean completed;
    private final boolean abort;
    private final String abortMessage;

    @Singular
    private final List<String> alerts;
    @Singular
    private final List<String> tags;
    @Singular
    private final List<String> variables;
}
