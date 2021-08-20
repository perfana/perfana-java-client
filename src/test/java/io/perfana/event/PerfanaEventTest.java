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

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import nl.stokpop.eventscheduler.EventMessageBusSimple;
import nl.stokpop.eventscheduler.api.CustomEvent;
import nl.stokpop.eventscheduler.api.EventLogger;
import nl.stokpop.eventscheduler.api.config.TestConfig;
import nl.stokpop.eventscheduler.api.message.EventMessageBus;
import nl.stokpop.eventscheduler.log.EventLoggerStdOut;
import org.junit.Rule;
import org.junit.Test;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class PerfanaEventTest {

    private static final String REPLY_BODY_BENCHMARK_RESULTS = "{\n" +
        "    \"requirements\": {\n" +
        "        \"result\": true,\n" +
        "        \"deeplink\": \"http://localhost:4000/test-run/perfana-gatling-afterburner-6?application=Afterburner&testType=loadTest&testEnvironment=acc\"\n" +
        "    },\n" +
        "    \"benchmarkPreviousTestRun\": {\n" +
        "        \"result\": true,\n" +
        "        \"deeplink\": \"http://localhost:4000/test-run/perfana-gatling-afterburner-6?application=Afterburner&testType=loadTest&testEnvironment=acc\"\n" +
        "    },\n" +
        "    \"benchmarkBaselineTestRun\": {\n" +
        "        \"result\": true,\n" +
        "        \"deeplink\": \"http://localhost:4000/test-run/perfana-gatling-afterburner-6?application=Afterburner&testType=loadTest&testEnvironment=acc\"\n" +
        "    }\n" +
        "}";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    @Test
    public void testLifeCycle() {

        wireMockRule.stubFor(post(urlEqualTo("/api/events"))
                        .willReturn(aResponse()
                                .withBody("{ hello: world }")));

        wireMockRule.stubFor(post(urlEqualTo("/api/test"))
                        .willReturn(aResponse()
                                .withBody("{ \"abort\":false }")));

        wireMockRule.stubFor(get(urlPathMatching("/api/benchmark-results/.*"))
                        .willReturn(aResponse()
                                .withBody(REPLY_BODY_BENCHMARK_RESULTS)));

        PerfanaEventConfig eventConfig = new PerfanaEventConfig();
        eventConfig.setPerfanaUrl("http://localhost:" + wireMockRule.port());
        eventConfig.setName("test-name");
        eventConfig.setApiKey("perfana-api-key-123");
        eventConfig.setTestConfig(TestConfig.builder().build());

        EventLogger eventLogger = EventLoggerStdOut.INSTANCE;

        EventMessageBus messageBus = new EventMessageBusSimple();

        PerfanaEvent event = new PerfanaEvent(eventConfig.toContext(), messageBus, eventLogger);

        event.beforeTest();

        event.check();

        event.customEvent(new CustomEvent(Duration.ofMillis(2), "hello-event", "test event 123"));

        event.abortTest();

        // this will not happen, either abortTest or afterTest is called by event-scheduler, not both
        // event.afterTest();

    }
}
