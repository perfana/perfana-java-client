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
package io.perfana.client.api;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestContextBuilderTest {

    @Test
    public void testTestContextBuilderContents() {
        String ciBuildResultsUrl = "http://url";
        String version = "release-xyz";

        Map<String,String> variables = new HashMap<>();
        variables.put("var1", "varValue1");

        Map<String,String> variablesExpected = new HashMap<>();
        variablesExpected.put("var1", "varValue1");

        String annotation34 = "annotation34";

        String testType = "testType";
        String testEnv = "testEnv";
        String testRunId = "testRunId";
        String rampupTimeSeconds = "10";
        String constantLoadTimeInSeconds = "300";

        String tag6767 = "tag6767";
        String tag3434 = "tag3434";

        String tagsSeparatedByCommas = tag6767 + "," + tag3434;

        TestContext context = new TestContextBuilder()
            .setWorkload(testType)
            .setTestEnvironment(testEnv)
            .setTestRunId(testRunId)
            .setCIBuildResultsUrl(ciBuildResultsUrl)
            .setVersion(version)
            .setRampupTimeInSeconds(rampupTimeSeconds)
            .setConstantLoadTimeInSeconds(constantLoadTimeInSeconds)
            .setAnnotations(annotation34)
            .setVariables(variables)
            .setTags(tagsSeparatedByCommas)
            .build();

        assertEquals(ciBuildResultsUrl, context.getCIBuildResultsUrl());
        assertEquals(version, context.getVersion());
        assertEquals(variablesExpected, context.getVariables());
        assertEquals(annotation34, context.getAnnotations());
        assertEquals(testType, context.getWorkload());
        assertEquals(testEnv, context.getTestEnvironment());
        assertEquals(testRunId, context.getTestRunId());
        assertEquals(tag6767, context.getTags().get(0));
        assertEquals(tag3434, context.getTags().get(1));
        assertEquals(Integer.parseInt(rampupTimeSeconds), context.getRampupTime().getSeconds());
        assertEquals(Integer.parseInt(constantLoadTimeInSeconds), context.getPlannedDuration().getSeconds());
    }

}