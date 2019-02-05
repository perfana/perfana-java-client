package io.perfana.test;

import io.perfana.client.PerfanaClient;
import io.perfana.client.PerfanaClientBuilder;
import io.perfana.client.api.PerfanaClientLogger;
import io.perfana.client.api.PerfanaClientLoggerStdOut;
import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.PerfanaConnectionSettingsBuilder;
import io.perfana.client.api.PerfanaTestContext;
import io.perfana.client.api.PerfanaTestContextBuilder;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * This test class is in another package to check access package private fields.
 */
public class PerfanaClientTest
{
    @Test
    public void create() {

        PerfanaClientLogger testLogger = new PerfanaClientLoggerStdOut();

        String eventSchedule =
                "   \n" +
                "    PT1S  |restart   (   restart to reset replicas  )   |{ 'server':'myserver' 'replicas':2, 'tags': [ 'first', 'second' ] }    \n" +
                "PT600S   |scale-down |   { 'replicas':1 }   \n" +
                "PT660S|    heapdump|server=    myserver.example.com;   port=1567  \n" +
                "   PT900S|scale-up|{ 'replicas':2 }\n" +
                "  \n";

        PerfanaConnectionSettings settings = new PerfanaConnectionSettingsBuilder()
                .setPerfanaUrl("http://perfUrl")
                .setRetryMaxCount("5")
                .setRetryTimeInSeconds("3")
                .build();

        PerfanaTestContext context = new PerfanaTestContextBuilder()
                .setTestType("testType")
                .setTestEnvironment("testEnv")
                .setTestRunId("testRunId")
                .setCIBuildResultsUrl("http://url")
                .setApplicationRelease("release")
                .setRampupTimeInSeconds("10")
                .setConstantLoadTimeInSeconds("300")
                .setAnnotations("annotation")
                .setVariables(new HashMap<>())
                .build();

        PerfanaClient client = new PerfanaClientBuilder()
                .setPerfanaConnectionSettings(settings)
                .setPerfanaTestContext(context)
                .setAssertResultsEnabled(true)
                .setLogger(testLogger)
                .addEventProperty("myClass", "name", "value")
                .setCustomPerfanaEvents(null)
                .build();

        assertNotNull(client);
        assertEquals("http://perfUrl", settings.getPerfanaUrl());

//        client.startSession();
//        client.stopSession();
    }

    /**
     * Regression: no exceptions expected feeding null
     */
    @Test
    public void createWithNulls() {

        PerfanaTestContext context = new PerfanaTestContextBuilder()
                .setAnnotations(null)
                .setApplicationRelease(null)
                .setApplication(null)
                .setCIBuildResultsUrl(null)
                .setConstantLoadTimeInSeconds(null)
                .setConstantLoadTime(null)
                .setRampupTimeInSeconds(null)
                .setRampupTime(null)
                .setTestEnvironment(null)
                .setTestRunId(null)
                .setTestType(null)
                .setVariables(null)
                .build();

        PerfanaConnectionSettings settings = new PerfanaConnectionSettingsBuilder()
                .setPerfanaUrl(null)
                .setRetryMaxCount(null)
                .setRetryTimeInSeconds(null)
                .setKeepAliveInterval(null)
                .setKeepAliveTimeInSeconds(null)
                .setRetryDuration(null).build();

        new PerfanaClientBuilder()
                .setPerfanaTestContext(context)
                .setPerfanaConnectionSettings(settings)
                .setCustomPerfanaEvents(null)
                .setLogger(null)
                .setBroadcaster(null)
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
}
