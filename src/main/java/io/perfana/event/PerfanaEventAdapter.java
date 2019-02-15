package io.perfana.event;

import io.perfana.client.api.TestContext;

/**
 * Adapter class with empty method implementations of the PerfanaEvent interface.
 * Extend this class so you only have to implement the methods that are used.
 *
 * Always provide a proper name for a PerfanaEvent for traceability.
 */
public abstract class PerfanaEventAdapter implements PerfanaEvent {

    @Override
    public void beforeTest(TestContext context, EventProperties properties) {

    }

    @Override
    public void afterTest(TestContext context, EventProperties properties) {

    }

    @Override
    public void keepAlive(TestContext context, EventProperties properties) {

    }

    @Override
    public void customEvent(TestContext context, EventProperties properties, ScheduleEvent scheduleEvent) {

    }
}
