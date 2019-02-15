package io.perfana.event.generator;

import io.perfana.client.api.TestContext;
import io.perfana.event.EventScheduleGenerator;
import io.perfana.event.ScheduleEvent;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EventScheduleGeneratorDefault implements EventScheduleGenerator {

    private static final String EVENT_SCHEDULE_TAG = "eventSchedule";

    @Override
    public List<ScheduleEvent> generateEvents(TestContext context, GeneratorProperties properties) {
        return createPerfanaTestEvents(properties.getProperty(EVENT_SCHEDULE_TAG));
    }

    public List<ScheduleEvent> createPerfanaTestEvents(String eventsAsString) {
        if (eventsAsString != null) {
            BufferedReader eventReader = new BufferedReader(new StringReader(eventsAsString));
            List<String> events = eventReader.lines()
                    .map(String::trim)
                    .filter(e -> !e.isEmpty())
                    .collect(Collectors.toList());
            return parseScheduleEvents(events);
        }
        else {
            return Collections.emptyList();
        }
    }

    private List<ScheduleEvent> parseScheduleEvents(List<String> eventSchedule) {
        return eventSchedule.stream()
                .map(ScheduleEvent::createFromLine)
                .collect(Collectors.toList());
    }


}
