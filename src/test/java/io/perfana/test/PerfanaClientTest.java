package io.perfana.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.perfana.client.PerfanaClient;
import io.perfana.client.PerfanaClientBuilder;
import org.junit.Test;

import java.util.Properties;

/**
 * Put in another package to check access package private fields.
 */
public class PerfanaClientTest
{
    @Test
    public void create()
    {
        PerfanaClient.Logger testLogger = new PerfanaClient.Logger() {
            @Override
            public void info(final String message) {
                System.out.println("test info; " + message);
            }

            @Override
            public void warn(final String message) {
                System.out.println("test warn; " + message);
            }

            @Override
            public void error(final String message) {
                System.out.println("test error; " + message);
            }

            @Override
            public void debug(final String message) {
                System.out.println("test debug; " + message);
            }
        };
        
        PerfanaClient client =
                new PerfanaClientBuilder()
                        .setApplication("application")
                        .setTestType("testType")
                        .setTestEnvironment("testEnv")
                        .setTestRunId("testRunId")
                        .setCIBuildResultsUrl("url")
                        .setApplicationRelease("release")
                        .setRampupTimeInSeconds("10")
                        .setConstantLoadTimeInSeconds("60")
                        .setPerfanaUrl("perfUrl")
                        .setAnnotations("annotations")
                        .setVariables(new Properties())
                        .setAssertResultsEnabled(true)
                        .setLogger(testLogger)
                        .createPerfanaClient();

        assertNotNull(client);
    }
}
