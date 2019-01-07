package io.perfana.test;

import io.perfana.client.PerfanaClient;
import io.perfana.client.PerfanaClientBuilder;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;

/**
 * Put in another package to check access package private fields.
 */
public class PerfanaClientTest
{
    @Test
    public void create() {
        PerfanaClient.Logger testLogger = new PerfanaClient.Logger() {
            @Override
            public void info(final String message) {
                say("INFO ", message);
            }

            @Override
            public void warn(final String message) {
                say("WARN ", message);
            }

            @Override
            public void error(final String message) {
                say("ERROR", message);
            }

            @Override
            public void debug(final String message) {
                say("DEBUG", message);
            }

            private void say(String level, String something) {
                System.out.printf("## %s ## %s%n", level, something);
            }
        };
        
        PerfanaClient client =
                new PerfanaClientBuilder()
                        .setApplication("application")
                        .setTestType("testType")
                        .setTestEnvironment("testEnv")
                        .setTestRunId("testRunId")
                        .setCIBuildResultsUrl("http://url")
                        .setApplicationRelease("release")
                        .setRampupTimeInSeconds("10")
                        .setConstantLoadTimeInSeconds("60")
                        .setPerfanaUrl("http://perfUrl")
                        .setAnnotations("annotations")
                        .setVariables(new Properties())
                        .setAssertResultsEnabled(true)
                        .setLogger(testLogger)
                        .addEventProperty("myClass", "name", "value")
                        .createPerfanaClient();

        assertNotNull(client);

//        client.startSession();
//        client.stopSession();
    }
}
