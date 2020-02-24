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
package io.perfana.event;

import io.perfana.client.api.PerfanaClientLoggerStdOut;
import io.perfana.client.api.TestContext;
import io.perfana.client.api.TestContextBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PerfanaEventProviderTest {

    @Test
    public void broadcastCustomEventWithFailureShouldProceed() {

        // not multi-threading code, but used as a convenience to change an object in the inner classes below
        // beware: expects a certain order for the events to be called, which can be different depending on implementation
        final AtomicInteger counter = new AtomicInteger(0);

        List<PerfanaEvent> events = new ArrayList<>();
        // this should succeed
        events.add(new MyTestEventThatCanFail(counter, 0, 1));
        // this will fail: counter is 0
        events.add(new MyTestEventThatCanFail(counter, 10, 11));
        // this should succeed
        events.add(new MyTestEventThatCanFail(counter, 1, 2));

        PerfanaEventProvider provider = new PerfanaEventProvider(events, new PerfanaClientLoggerStdOut());

        provider.broadcastCustomEvent(new TestContextBuilder().build(), new PerfanaEventProperties(), ScheduleEvent.createFromLine("PT1M|test-event"));

        assertEquals("counter should be set to 2 even though the middle event failed", 2, counter.intValue());
    }

    private static class MyTestEventThatCanFail extends PerfanaEventAdapter {
        private AtomicInteger counter;
        private int expectValue;
        private int newValue;
        MyTestEventThatCanFail(AtomicInteger counter, int expectValue, int newValue) {
            this.counter = counter;
            this.expectValue= expectValue;
            this.newValue = newValue;
        }
        @Override
        public String getName() {
            return "MyTestEventThatCanFail";
        }
        @Override
        public void customEvent(TestContext context, EventProperties properties, ScheduleEvent scheduleEvent) {
            if (!counter.compareAndSet(expectValue, newValue)) throw new RuntimeException("counter was not " + expectValue);
        }
    }

}