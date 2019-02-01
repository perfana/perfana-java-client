package io.perfana.event;

import io.perfana.client.api.PerfanaClientLoggerStdOut;
import io.perfana.client.api.PerfanaTestContext;
import io.perfana.client.api.PerfanaTestContextBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

public class PerfanaEventProviderTest {

    @Test
    public void broadcastCustomEventWithFailureShouldProcede() {

        // not multithreading but used as a convenience to change an object in the inner classes below
        // beware: expects a certain order for the events to be called, which can be different depending on implementation
        final AtomicInteger counter = new AtomicInteger(0);

        List<PerfanaTestEvent> events = new ArrayList<>();
        events.add(new PerfanaTestEventAdapter() {
            @Override
            public String name() {
                return "MyTestEventThatFails";
            }
            @Override
            public void customEvent(PerfanaTestContext context, Map<String, String> eventProperties, ScheduleEvent scheduleEvent) {
                if (!counter.compareAndSet(0, 1)) throw new RuntimeException("counter was not 0");
                throw new RuntimeException("This custom event failed!");
            }
        });
        events.add(new PerfanaTestEventAdapter() {
            @Override
            public String name() {
                return "MyTestEventThatShouldRun";
            }
            @Override
            public void customEvent(PerfanaTestContext context, Map<String, String> eventProperties, ScheduleEvent scheduleEvent) {
                if (!counter.compareAndSet(1, 2)) throw new RuntimeException("counter was not 1");
            }
        });
        PerfanaEventProvider provider = new PerfanaEventProvider(events, new PerfanaClientLoggerStdOut());

        provider.broadcastCustomEvent(new PerfanaTestContextBuilder().build(), new PerfanaEventProperties(), ScheduleEvent.createFromLine("PT1M|test-event"));

        assertTrue("counter should be set to 2 even though the other event failed", counter.compareAndSet(2,3));
    }
}