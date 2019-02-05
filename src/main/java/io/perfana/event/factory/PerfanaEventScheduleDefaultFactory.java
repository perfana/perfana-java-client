package io.perfana.event.factory;

import io.perfana.client.api.PerfanaTestContext;
import io.perfana.event.ScheduleEvent;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PerfanaEventScheduleDefaultFactory implements PerfanaEventScheduleFactory {

    @Override
    public List<ScheduleEvent> createPerfanaTestEvents(PerfanaTestContext context, Map<String, String> parameters) {
        return createPerfanaTestEvents(parameters.get("eventSchedule"));
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
