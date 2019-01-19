package io.perfana.event;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleEvent {
    private Duration duration;
    private String name;
    private String settings;

    public ScheduleEvent(final Duration duration, final String name, final String settings) {
        this.duration = duration;
        this.name = name;
        this.settings = settings;
    }

    public ScheduleEvent(final Duration duration, final String name) {
        this.duration = duration;
        this.name = name;
        this.settings = null;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public String getSettings() {
        return settings;
    }

    /**
     * @param line line that is separated by duration|eventname|settings(optional)
     * @return new ScheduleEvent
     */
    public static ScheduleEvent createFromLine(String line) {

        if (line == null || line.trim().isEmpty()) {
            throw new ScheduleEventWrongFormat("empty line: [" + line + "]");
        }

        if (!line.contains("|")) {
            throw new ScheduleEventWrongFormat("line should contain at least a duration and event name, separated by '|': [" + line + "]");
        }

        List<String> elements = Arrays.stream(line.split("\\|"))
                .map(String::trim)
                .collect(Collectors.toList());

        if (!(elements.size() == 2 || elements.size() == 3)) {
            throw new ScheduleEventWrongFormat("Wrong number of elements in line, expected 'duration|name|setting(optional)': [" + line + "]");
        }
        
        Duration duration;
        String textDuration = elements.get(0);
        String name = elements.get(1);

        try {
            duration = Duration.parse(textDuration);
        } catch (Exception e) {
            throw new ScheduleEventWrongFormat("Failed to parse duration: [" + textDuration + "] from line: [" + line + "]", e);
        }
        
        if (elements.size() == 2) {
            return new ScheduleEvent(duration, name);
        }
        else {
            String settings = elements.get(2);
            return new ScheduleEvent(duration, name, settings);
        }
    }

    @Override
    public String toString() {
        return settings == null ?
                String.format("ScheduleEvent %s [execute after=%s]", name, duration) :
                String.format("ScheduleEvent %s [execute after=%s settings=%s]", name, duration, settings);
    }

    public static class ScheduleEventWrongFormat extends RuntimeException {
        public ScheduleEventWrongFormat(String message) {
            super(message);
        }

        public ScheduleEventWrongFormat(String message, Exception e) {
           super(message, e);
        }
    }
}
