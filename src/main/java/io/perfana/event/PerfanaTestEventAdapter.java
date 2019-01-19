package io.perfana.event;

import io.perfana.client.api.PerfanaTestContext;

import java.util.Map;

/**
 * Adapter class with empty method implementations of the PerfanaTestEvent interface.
 * Extend this class so you only have to implement the methods that are used.
 */
public abstract class PerfanaTestEventAdapter implements PerfanaTestEvent {

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
