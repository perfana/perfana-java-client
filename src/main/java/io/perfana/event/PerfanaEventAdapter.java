package io.perfana.event;

import io.perfana.client.api.PerfanaTestContext;

import java.util.Map;

/**
 * Adapter class with empty method implementations of the PerfanaTestEvent interface.
 * Extend this class so you only have to implement the methods that are used.
 *
 * Always provide a proper name for a PerfanaTestEvent for traceability.
 */
public abstract class PerfanaEventAdapter implements PerfanaEvent {

    @Override
    public void beforeTest(PerfanaTestContext context, Map<String, String> eventProperties) {

    }

    @Override
    public void afterTest(PerfanaTestContext context, Map<String, String> eventProperties) {

    }

    @Override
    public void keepAlive(PerfanaTestContext context, Map<String, String> eventProperties) {

    }

    @Override
    public void customEvent(PerfanaTestContext context, Map<String, String> eventProperties, ScheduleEvent scheduleEvent) {

    }
}
