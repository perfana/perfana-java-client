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
package io.perfana.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import io.perfana.client.PerfanaClient;
import io.perfana.client.PerfanaClientBuilder;
import io.perfana.client.api.*;
import io.perfana.client.domain.*;
import io.perfana.client.exception.PerfanaAssertResultsException;
import io.perfana.client.exception.PerfanaAssertionsAreFalse;
import io.perfana.eventscheduler.exception.handler.AbortSchedulerException;
import io.perfana.eventscheduler.exception.handler.KillSwitchException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * This test class is in another package to check access package private fields.
 */
public class PerfanaClientTest
{
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String MESSAGE_THERE_WAS_A_FAILURE = "{\"message\":\"there was a failure!\"}";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    @Test
    public void create() {
        PerfanaClient client = createPerfanaClient();
        assertNotNull(client);
    }

    private PerfanaClient createPerfanaClient() {
        PerfanaClientLogger testLogger = new PerfanaClientLoggerStdOut();

        PerfanaConnectionSettings settings = new PerfanaConnectionSettingsBuilder()
                .setPerfanaUrl("http://localhost:" + wireMockRule.port())
                .setRetryMaxCount("5")
                .setRetryTimeInSeconds("3")
                .build();

        TestContext context = new TestContextBuilder()
                .setWorkload("testType")
                .setTestEnvironment("testEnv")
                .setTestRunId("testRunId")
                .setCIBuildResultsUrl("http://url")
                .setVersion("release")
                .setRampupTimeInSeconds("10")
                .setConstantLoadTimeInSeconds("300")
                .setAnnotations("annotation")
                .setVariables(Collections.emptyMap())
                .setTags("")
                .build();

        return new PerfanaClientBuilder()
                .setPerfanaConnectionSettings(settings)
                .setTestContext(context)
                .setAssertResultsEnabled(true)
                .setLogger(testLogger)
                .build();
    }

    /**
     * Regression: no exceptions expected feeding null
     */
    @Test
    public void createWithNulls() {

        TestContext context = new TestContextBuilder()
                .setAnnotations(null)
                .setVersion(null)
                .setSystemUnderTest(null)
                .setCIBuildResultsUrl(null)
                .setConstantLoadTimeInSeconds(null)
                .setConstantLoadTime(null)
                .setRampupTimeInSeconds(null)
                .setRampupTime(null)
                .setTestEnvironment(null)
                .setTestRunId(null)
                .setWorkload(null)
                .setVariables((Properties)null)
                .setTags((String)null)
                .build();

        PerfanaConnectionSettings settings = new PerfanaConnectionSettingsBuilder()
                .setPerfanaUrl(null)
                .setRetryMaxCount(null)
                .setRetryTimeInSeconds(null)
                .setRetryDuration(null).build();

        new PerfanaClientBuilder()
                .setTestContext(context)
                .setPerfanaConnectionSettings(settings)
                .build();

    }

    @Test
    public void createWithFail() {

        PerfanaConnectionSettings settings =
                new PerfanaConnectionSettingsBuilder()
                        .setRetryTimeInSeconds("P5")
                        .build();

        assertNotNull(settings);

    }

    @Test
    public void createJsonMessage() {
        Map<String, String> vars = new HashMap<>();

        String var1 = "hostname";
        String value1 = "foo.com";
        vars.put(var1, value1);

        String var2 = "env";
        String value2 = "performance-test-2-env";

        String tag1 = "   tag-1 ";
        String tag2 = " tag-2  ";
        String tags = String.join(",", Arrays.asList(tag1, tag2));
        vars.put(var2, value2);

        String annotations = "Xmx set to 2g";
        TestContext context = new TestContextBuilder()
                .setAnnotations(annotations)
                .setVariables(vars)
                .setTags(tags)
                .build();

        Map<String, String> extraVariables = new HashMap<>();
        extraVariables.put("extraVar1", "extraValue1");
        extraVariables.put("extraVar2", "extraValue2");
        String json = PerfanaClient.perfanaMessageToJson(context, false, extraVariables);

        assertTrue(json.contains(annotations));
        assertTrue(json.contains(var1));
        assertTrue(json.contains(var2));
        assertTrue(json.contains(value1));
        assertTrue(json.contains(value2));
        assertTrue(json.contains(tag1.trim()));
        assertTrue(json.contains(tag2.trim()));
        assertTrue(json.contains("extraVar1"));
        assertTrue(json.contains("extraVar2"));

    }

    @Test
    public void testVarsMissing() {
        /*
         * <variables>
         *    <property>
         *        <name>foo</name>
         *        <value>1</value>
         *    </property>
         *    <property>
         *        <name>bar</name>
         *        <value>2</value>
         *    </property>
         * </variables>
         */
        Properties props = new Properties();
        props.put("foo", "foo-1");
        props.put("bar", "bar-2");
        
        TestContext context = new TestContextBuilder()
                .setVariables(props)
                .build();

        String json = PerfanaClient.perfanaMessageToJson(context, false, Collections.emptyMap());
        assertTrue(json.contains("foo"));
        assertTrue(json.contains("bar"));
        assertTrue(json.contains("foo-1"));
        assertTrue(json.contains("bar-2"));
    }

    @Test(expected = KillSwitchException.class)
    public void testPerfanaTestCallWithResult() {
        wireMockRule.stubFor(post(urlEqualTo("/api/test"))
                .willReturn(aResponse()
                        .withBody("{ \"abort\": true, \"abortMessage\": \"What is wrong?\" }")));

        PerfanaClient perfanaClient = createPerfanaClient();
        TestContext testContext = new TestContextBuilder().build();
        perfanaClient.callPerfanaTestEndpoint(testContext, false);
    }

    @Test
    public void testPerfanaTestCallWithResultCompletedTrue() {
        UrlPattern urlPattern = urlEqualTo("/api/test");
        wireMockRule.stubFor(post(urlPattern)
            .willReturn(aResponse()
                .withBody("{ \"abort\": true, \"abortMessage\": \"What is wrong?\" }")));

        PerfanaClient perfanaClient = createPerfanaClient();
        TestContext testContext = new TestContextBuilder().build();
        // should not throw KillSwitchException when completed is true (not a keep alive call)
        perfanaClient.callPerfanaTestEndpoint(testContext, true);

        verify(postRequestedFor(urlPattern));
    }

    @Test
    public void testPerfanaAssertResultsCall() throws Exception {

        Benchmark benchmark = Benchmark.builder()
            .requirements(Result.builder().result(true).deeplink("https://perfana:4000/requirements/123").build())
            .benchmarkBaselineTestRun(Result.builder().result(true).deeplink("https://perfana:4000/benchmarkBaseline/123").build())
            .benchmarkPreviousTestRun(Result.builder().result(true).deeplink("https://perfana:4000/benchmarkPrevious/123").build())
            .build();

        String body = OBJECT_MAPPER.writeValueAsString(benchmark);

        wireMockRule.stubFor(get(urlEqualTo("/api/benchmark-results/unknown/testRunId"))
            .willReturn(aResponse()
                .withBody(body)));

        PerfanaClient perfanaClient = createPerfanaClient();
        String assertResults = perfanaClient.assertResults();

        String expectReply = "All configured checks are OK: \n" +
            "https://perfana:4000/requirements/123\n" +
            "https://perfana:4000/benchmarkBaseline/123\n" +
            "https://perfana:4000/benchmarkPrevious/123";

        assertEquals(expectReply, assertResults);
    }

    @Test(expected = PerfanaAssertionsAreFalse.class)
    public void testPerfanaAssertResultsFailedCall() throws Exception {

        Benchmark benchmark = Benchmark.builder()
            .requirements(Result.builder().result(false).deeplink("https://perfana:4000/requirements/123").build())
            .benchmarkBaselineTestRun(Result.builder().result(false).deeplink("https://perfana:4000/benchmarkBaseline/123").build())
            .benchmarkPreviousTestRun(Result.builder().result(true).deeplink("https://perfana:4000/benchmarkPrevious/123").build())
            .build();

        String body = OBJECT_MAPPER.writeValueAsString(benchmark);

        wireMockRule.stubFor(get(urlEqualTo("/api/benchmark-results/unknown/testRunId"))
            .willReturn(aResponse()
                .withBody(body)));

        PerfanaClient perfanaClient = createPerfanaClient();
        perfanaClient.assertResults();

    }

    @Test(expected = AbortSchedulerException.class)
    public void testPerfanaTestCallWithoutAuth() {
        wireMockRule.stubFor(post(urlEqualTo("/api/test"))
            .willReturn(aResponse()
                .withStatus(401)
                .withHeader("WWW-Authenticate", "Bearer realm=\"Users\", error=\"invalid_token\", error_description=\"Invalid or expired API key\"")
                .withStatusMessage("Unauthorized")
                .withBody("Unauthorized")));

        PerfanaClient perfanaClient = createPerfanaClient();
        TestContext testContext = new TestContextBuilder().build();
        try {
            perfanaClient.callPerfanaTestEndpoint(testContext, false);
        } catch (AbortSchedulerException e) {
            // System.out.println("Error: " + e);
            assertTrue(e.getMessage().contains("Invalid or expired API key"));
            throw e;
        }
    }

    @Test(expected = PerfanaAssertResultsException.class)
    @Ignore("takes too long to test timeouts, ignore test, enable to test manually")
    public void testPerfanaTestCallWithoutTimeout() throws Exception {
        wireMockRule.stubFor(get(urlEqualTo("/api/benchmark-results/unknown/testRunId"))
            .willReturn(aResponse()
                .withFixedDelay(10_000)
                .withStatus(200)
                .withBody("{'message': 'all ok'}")));

        PerfanaClient perfanaClient = createPerfanaClient();
        perfanaClient.assertResults();
    }

    @Test(expected = PerfanaAssertResultsException.class)
    public void testPerfanaTestCallWith500() throws Exception {
        wireMockRule.stubFor(get(urlEqualTo("/api/benchmark-results/unknown/testRunId"))
            .willReturn(aResponse()
                .withStatus(500)
                .withBody(MESSAGE_THERE_WAS_A_FAILURE)));

        PerfanaClient perfanaClient = createPerfanaClient();

        // check if the message is ok, is there a better way to check this?
        try {
            perfanaClient.assertResults();
        } catch (PerfanaAssertResultsException e) {
            // System.out.println("Error: " + e);
            assertTrue(e.getMessage().contains("due to: there was a failure!"));
            throw e;
        }
    }

    @Test(expected = PerfanaAssertResultsException.class)
    public void testPerfanaTestCallWith400() throws Exception {
        wireMockRule.stubFor(get(urlEqualTo("/api/benchmark-results/unknown/testRunId"))
            .willReturn(aResponse()
                .withStatus(400)
                .withBody("{\"message\":\"Match error: Missing key 'systemUnderTest'\",\"path\":\"\",\"sanitizedError\":{\"isClientSafe\":true,\"error\":400,\"reason\":\"Match failed\",\"message\":\"Match failed [400]\",\"errorType\":\"Meteor.Error\"},\"errorType\":\"Match.Error\"}")));

        PerfanaClient perfanaClient = createPerfanaClient();
        try {
            perfanaClient.assertResults();
        } catch (PerfanaAssertResultsException e) {
            System.out.println("Error: " + e);
            assertTrue(e.getMessage().contains("Missing key 'systemUnderTest'"));
            throw e;
        }

    }

    @Test(expected = PerfanaAssertResultsException.class)
    @Ignore("takes too long to test timeouts, ignore test, enable to test manually")
    public void testPerfanaTestCallWith503() throws Exception {
        wireMockRule.stubFor(get(urlEqualTo("/api/benchmark-results/unknown/testRunId"))
            .willReturn(aResponse()
                .withStatus(503)
                .withBody(MESSAGE_THERE_WAS_A_FAILURE)));

        PerfanaClient perfanaClient = createPerfanaClient();
        String results = perfanaClient.assertResults();
        fail(results);
    }

    @Test
    public void testRunConfigJson() {
        UrlPattern urlPattern = urlEqualTo("/api/config/json");

        wireMockRule.stubFor(post(urlPattern)
            .willReturn(aResponse()
                .withStatus(200)));

        PerfanaClient perfanaClient = createPerfanaClient();

        List<String> tags = new ArrayList<>();
        tags.add("tag1");
        tags.add("tag2");

        List<String> includes = new ArrayList<>();
        includes.add("include1");
        includes.add("include2");

        List<String> excludes = new ArrayList<>();
        excludes.add("exclude1");
        excludes.add("exclude2");

        perfanaClient.addTestRunConfigJson(new TestRunConfigJson("app", "env", "loadTest", "test-123", tags, includes, excludes, "{ \"hello\": 123 }"));

        verify(postRequestedFor(urlPattern));

    }

    @Test
    public void testRunConfigKeyValue() {
        UrlPattern urlPattern = urlEqualTo("/api/config/key");

        wireMockRule.stubFor(post(urlPattern)
                .willReturn(aResponse()
                        .withStatus(200)));

        PerfanaClient perfanaClient = createPerfanaClient();

        List<String> tags = new ArrayList<>();
        tags.add("tag1");
        tags.add("tag2");

        perfanaClient.addTestRunConfigKeyValue(new TestRunConfigKeyValue("app", "env", "loadTest", "test-123", tags, "key1", "value1"));

        verify(postRequestedFor(urlPattern));
    }

    @Test
    public void testRunConfigKeyKeys() {
        UrlPattern urlPattern = urlEqualTo("/api/config/keys");

        wireMockRule.stubFor(post(urlPattern)
                .willReturn(aResponse()
                        .withStatus(200)));

        PerfanaClient perfanaClient = createPerfanaClient();

        List<String> tags = new ArrayList<>();
        tags.add("tag1");
        tags.add("tag2");

        List<ConfigItem> configItems = new ArrayList<>();
        configItems.add(new ConfigItem("key1", "value1"));
        configItems.add(new ConfigItem("key2", "value2"));

        perfanaClient.addTestRunConfigKeys(new TestRunConfigKeys("app", "env", "loadTest", "test-123", tags, configItems));

        verify(postRequestedFor(urlPattern));
    }
}
