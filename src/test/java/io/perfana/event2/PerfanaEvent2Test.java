package io.perfana.event2;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import nl.stokpop.eventscheduler.api.*;
import nl.stokpop.eventscheduler.log.EventLoggerStdOut;
import org.junit.Rule;
import org.junit.Test;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class PerfanaEvent2Test {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8888));

    @Test
    public void testLiveCycle() {

        wireMockRule.stubFor(post(urlEqualTo("/events"))
                        .willReturn(aResponse()
                                .withBody("{ hello: world }")));

        wireMockRule.stubFor(post(urlEqualTo("/test"))
                        .willReturn(aResponse()
                                .withBody("{ hello: world }")));

        wireMockRule.stubFor(get(urlPathMatching("/get-benchmark-results/.*"))
                        .willReturn(aResponse()
                                .withBody("{ 'requirements': {" +
                                        "         'result': true } }")));

        TestContext testContext = new TestContextBuilder().build();
        EventProperties eventProperties = new EventProperties();
        EventLogger eventLogger = EventLoggerStdOut.INSTANCE;

        PerfanaEvent2 event = new PerfanaEvent2("text", testContext, eventProperties, eventLogger);

        event.beforeTest();

        event.check();

        event.customEvent(new CustomEvent(Duration.ofMillis(2), "hello-event", "test event 123"));

        event.abortTest();

        event.afterTest();

    }
}
