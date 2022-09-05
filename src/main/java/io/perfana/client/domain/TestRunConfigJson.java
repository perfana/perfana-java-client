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

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.*;

import java.util.List;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TestRunConfigJson {
    String application;
    String testEnvironment;
    String testType;
    String testRunId;
    @Singular
    List<String> tags;
    @Singular("includeItem")
    List<String> include;
    @Singular("excludeItem")
    List<String> exclude;
    @JsonRawValue
    String json;
}