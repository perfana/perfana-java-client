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
