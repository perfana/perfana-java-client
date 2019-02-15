package io.perfana.client;

import io.perfana.client.api.PerfanaConnectionSettingsBuilder;
import io.perfana.client.api.TestContextBuilder;
import org.junit.Test;

public class PerfanaClientBuilderTest {

    @Test
    public void createWithAlternativeClass() {
         String alternativeClassCustomEvents = "  @generator-class=io.perfana.event.generator.EventScheduleGeneratorDefault \n" +
                 "  eventSchedule=PT1M|do-something \n";

         PerfanaClientBuilder perfanaClientBuilder = new PerfanaClientBuilder()
                 .setCustomEvents(alternativeClassCustomEvents)
                 .setTestContext(new TestContextBuilder().build())
                 .setPerfanaConnectionSettings(new PerfanaConnectionSettingsBuilder().build());


        PerfanaClient perfanaClient = perfanaClientBuilder.build();
        
        // TODO what to assert?

    }

}