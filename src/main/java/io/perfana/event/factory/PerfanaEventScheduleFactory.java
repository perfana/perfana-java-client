package io.perfana.event.factory;

import io.perfana.client.api.PerfanaTestContext;
import io.perfana.event.ScheduleEvent;

import java.util.List;
import java.util.Map;

/**
 * Create a custom event schedule for Perfana events.
 *
 * Implement in your own code, use your own input files and/or logic to generate events.
 */
public interface PerfanaEventScheduleFactory {
    List<ScheduleEvent> createPerfanaTestEvents(PerfanaTestContext context, Map<String,String> eventProperties);
}
