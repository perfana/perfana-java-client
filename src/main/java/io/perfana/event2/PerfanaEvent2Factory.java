package io.perfana.event2;

import nl.stokpop.eventscheduler.api.*;

public class PerfanaEvent2Factory implements EventFactory {

    @Override
    public Event create(String eventName, TestContext testContext, EventProperties eventProperties, EventLogger logger) {
        return new PerfanaEvent2(eventName, testContext, eventProperties, logger);
    }
}
