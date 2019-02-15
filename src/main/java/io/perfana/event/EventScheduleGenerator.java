package io.perfana.event;

import io.perfana.client.api.TestContext;
import io.perfana.event.generator.GeneratorProperties;

import java.util.List;

/**
 * Create a custom event schedule for events.
 *
 * Implement in your own code, use your own input files and/or logic to generate events.
 */
public interface EventScheduleGenerator {
    List<ScheduleEvent> generateEvents(TestContext context, GeneratorProperties properties);
}
