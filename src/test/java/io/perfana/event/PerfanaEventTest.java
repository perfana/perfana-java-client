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

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import nl.stokpop.eventscheduler.api.CustomEvent;
import nl.stokpop.eventscheduler.api.EventLogger;
import nl.stokpop.eventscheduler.api.config.TestConfig;
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

        wireMockRule.stubFor(post(urlEqualTo("/events"))
                        .willReturn(aResponse()
                                .withBody("{ hello: world }")));

        wireMockRule.stubFor(post(urlEqualTo("/test"))
                        .willReturn(aResponse()
                                .withBody("{ \"abort\":false }")));

        wireMockRule.stubFor(get(urlPathMatching("/get-benchmark-results/.*"))
                        .willReturn(aResponse()
                                .withBody(REPLY_BODY_BENCHMARK_RESULTS)));

        PerfanaEventConfig eventConfig = new PerfanaEventConfig();
        eventConfig.setPerfanaUrl("http://localhost:" + wireMockRule.port());
        eventConfig.setName("test-name");
        eventConfig.setTestConfig(TestConfig.builder().build());

        EventLogger eventLogger = EventLoggerStdOut.INSTANCE;

        PerfanaEvent event = new PerfanaEvent(eventConfig, eventLogger);

        event.beforeTest();

        event.check();

        event.customEvent(new CustomEvent(Duration.ofMillis(2), "hello-event", "test event 123"));

        event.abortTest();

        // this will not happen, either abortTest or afterTest is called by event-scheduler, not both
        // event.afterTest();

    }
}
