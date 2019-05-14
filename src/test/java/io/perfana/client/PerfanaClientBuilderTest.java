package io.perfana.client;

import io.perfana.client.api.PerfanaConnectionSettingsBuilder;
import io.perfana.client.api.TestContextBuilder;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

public class PerfanaClientBuilderTest {

    @Test
    public void createWithAlternativeClass() {
         String alternativeClassCustomEvents = "  @generator-class=io.perfana.event.generator.EventScheduleGeneratorDefault \n" +
                 "  eventSchedule=PT1M|do-something \n";

         PerfanaClientBuilder perfanaClientBuilder = new PerfanaClientBuilder()
                 .setCustomEvents(alternativeClassCustomEvents)
                 .setTestContext(new TestContextBuilder().build())
                 .setPerfanaConnectionSettings(new PerfanaConnectionSettingsBuilder().build());

        PerfanaClient perfanaClient = perfanaClientBuilder.build(new URLClassLoader(new URL[]{}, Thread.currentThread().getContextClassLoader()));

        // TODO what to assert?

    }

}