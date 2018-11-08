package io.perfana.client;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Properties;

public class PerfanaClientTest
{
    @Test
    public void create()
    {
        PerfanaClient client = new PerfanaClient(
                "application",
                "testType",
                "testEnv",
                "testRunId",
                "url",
                "release",
                "10",
                "60",
                "perfUrl",
                "annotations",
                new Properties(),
                true
        );

        assertTrue( client.equals(client) );
    }
}
