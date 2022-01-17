/*
 *    Copyright 2020-2022  Peter Paul Bakker @ Perfana.io, Daniel Moll @ Perfana.io
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
    String id;
    String systemUnderTest;
    String testRunId;
    String version;
    String testEnvironment;
    String workload;
    @JsonProperty("CIBuildResultsUrl")
    String cibuildResultsUrl;

    int duration;
    int plannedDuration;
    int rampUp;

    String start; // 2020-02-21T20:59:31.206Z
    String end; // 2020-02-21T20:59:31.206Z
    String expires; // 2050-02-13T20:59:31.206Z

    boolean completed;
    boolean abort;
    String abortMessage;

    @Singular
    List<Alert> alerts;
    @Singular
    List<String> tags;
    @Singular
    List<Variable> variables;
}
