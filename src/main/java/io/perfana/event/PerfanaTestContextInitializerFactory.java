package io.perfana.event;

import io.perfana.eventscheduler.api.EventLogger;
import io.perfana.eventscheduler.api.TestContextInitializer;
import io.perfana.eventscheduler.api.TestContextInitializerFactory;

public class PerfanaTestContextInitializerFactory implements TestContextInitializerFactory<PerfanaEventContext> {

    @Override
    public TestContextInitializer create(PerfanaEventContext context, EventLogger logger) {
        return new PerfanaTestContextInitializer(context, logger);
    }

    public String getEventContextClassname() {
        return PerfanaEventContext.class.getName();
    }
}
