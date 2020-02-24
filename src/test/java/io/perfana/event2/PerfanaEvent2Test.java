/**
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
package io.perfana.event2;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import nl.stokpop.eventscheduler.api.*;
import nl.stokpop.eventscheduler.log.EventLoggerStdOut;
import org.junit.Rule;
import org.junit.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static nl.stokpop.eventscheduler.api.EventProperties.PROP_FACTORY_CLASSNAME;

public class PerfanaEvent2Test {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    @Test
    public void testLiveCycle() {

        wireMockRule.stubFor(post(urlEqualTo("/events"))
                        .willReturn(aResponse()
                                .withBody("{ hello: world }")));

        wireMockRule.stubFor(post(urlEqualTo("/test"))
                        .willReturn(aResponse()
                                .withBody("{ \"abort\":false, hello: world }")));

        wireMockRule.stubFor(get(urlPathMatching("/get-benchmark-results/.*"))
                        .willReturn(aResponse()
                                .withBody("{ 'requirements': {" +
                                        "         'result': true } }")));

        TestContext testContext = new TestContextBuilder().build();

        Map<String, String> props = new HashMap<>();
        props.put("perfanaUrl", "http://localhost:" + wireMockRule.port());
        props.put(PROP_FACTORY_CLASSNAME, "should be set, to what?");

        EventProperties eventProperties = new EventProperties(props);
        EventLogger eventLogger = EventLoggerStdOut.INSTANCE;

        PerfanaEvent2 event = new PerfanaEvent2("text", testContext, eventProperties, eventLogger);

        event.beforeTest();

        event.check();

        event.customEvent(new CustomEvent(Duration.ofMillis(2), "hello-event", "test event 123"));

        event.abortTest();

        event.afterTest();

    }
}
