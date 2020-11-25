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