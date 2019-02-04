package io.perfana.client;

import io.perfana.client.api.PerfanaCaller;
import io.perfana.client.api.PerfanaClientLoggerStdOut;
import io.perfana.client.api.PerfanaTestContext;
import io.perfana.client.api.PerfanaTestContextBuilder;
import io.perfana.client.exception.PerfanaClientRuntimeException;
import io.perfana.event.PerfanaEventBroadcaster;
import io.perfana.event.PerfanaEventProperties;
import io.perfana.event.ScheduleEvent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class PerfanaExecutorEngineTest {

    @Test
    public void createEventScheduleMessage() {

        List<ScheduleEvent> events = new ArrayList<>();
        events.add(ScheduleEvent.createFromLine("PT1M|event(my description: phase 1(foo))|settings=true"));
        events.add(ScheduleEvent.createFromLine("PT2M|event(my description: phase 2(bar))|settings=true"));
        events.add(ScheduleEvent.createFromLine("PT3M|event(my description: phase 3(very long event description test))|settings=true"));

        String eventScheduleMessage = PerfanaExecutorEngine.createEventScheduleMessage(events);

        System.out.println(eventScheduleMessage);
        String search = "phase";
        assertEquals(3, PerfanaUtils.countOccurrences(search, eventScheduleMessage));

    }

    @Test
    public void runMultipleEventsWithExceptions() throws InterruptedException {

        List<ScheduleEvent> events = new ArrayList<>();
        events.add(ScheduleEvent.createFromLine("PT0.1S|my-event(phase 1)"));
        events.add(ScheduleEvent.createFromLine("PT0.2S|my-event(phase 2)"));
        events.add(ScheduleEvent.createFromLine("PT0.3S|my-event(phase 3)"));
        events.add(ScheduleEvent.createFromLine("PT0.4S|my-event(phase 4)"));
        events.add(ScheduleEvent.createFromLine("PT0.5S|my-event(phase 5)"));

        PerfanaExecutorEngine engine = new PerfanaExecutorEngine(new PerfanaClientLoggerStdOut());

        PerfanaTestContext context = new PerfanaTestContextBuilder().build();

        final AtomicInteger callerCount = new AtomicInteger(0);
        final AtomicInteger broadcastCount = new AtomicInteger(0);

        PerfanaCaller caller = new PerfanaCaller() {
            @Override
            public void callPerfanaEvent(PerfanaTestContext context, String eventDescription) {
                System.out.println("call perfana event: " + eventDescription);
                callerCount.incrementAndGet();
                if (callerCount.intValue() < 3) {
                    throw new PerfanaClientRuntimeException("help! callPerfanaEvent: " + eventDescription);
                }
            }

            @Override
            public void callPerfanaTestEndpoint(PerfanaTestContext context, boolean complete) {
                System.out.println("call perfana test endpoint");
            }
        };

        PerfanaEventBroadcaster broadcaster = new PerfanaEventBroadcaster() {
            @Override
            public void broadcastBeforeTest(PerfanaTestContext context, PerfanaEventProperties eventProperties) {
                System.out.println("broadcast: before test");
            }

            @Override
            public void broadcastAfterTest(PerfanaTestContext context, PerfanaEventProperties eventProperties) {
                System.out.println("broadcast: after test");
            }

            @Override
            public void broadCastKeepAlive(PerfanaTestContext context, PerfanaEventProperties eventProperties) {
                System.out.println("broadcast: keep alive");
            }

            @Override
            public void broadcastCustomEvent(PerfanaTestContext context, PerfanaEventProperties eventProperties, ScheduleEvent event) {
                System.out.println("broadcast: custom event: " + event);
                broadcastCount.incrementAndGet();
                if (broadcastCount.intValue() < 3) {
                    throw new PerfanaClientRuntimeException("help! broadcastCustomEvent: " + event) ;
                }
            }
        };

        engine.startCustomEventScheduler(caller, context, events, broadcaster, new PerfanaEventProperties());

        // check if all events are called
        Thread.sleep(600);

        engine.shutdownThreadsNow();
        
        assertEquals("expected 5 caller calls", 5, callerCount.intValue());
        assertEquals("expected 5 broadcast calls", 5, broadcastCount.intValue());
    }

}