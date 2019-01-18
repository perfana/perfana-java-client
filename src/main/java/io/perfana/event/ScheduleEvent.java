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

        if (line == null || line.isEmpty() || !line.contains("|")) {
            throw new ScheduleEventWrongFormat("line: [" + line + "]");
        }

        List<String> elements = Arrays.stream(line.split("\\|")).map(String::trim).collect(Collectors.toList());

        if (!(elements.size() == 2 || elements.size() == 3)) {
            throw new ScheduleEventWrongFormat("Wrong number of elements in line, expected 2 or 3 separated by '|': " + line);
        }
        
        Duration duration;
        try {
            duration = Duration.parse(elements.get(0));
        } catch (Exception e) {
            throw new ScheduleEventWrongFormat("Failed to parse duration: " + elements.get(0) + " from line: " + line, e);
        }

        String name = elements.get(1);
        
        if (elements.size() == 2) {
            return new ScheduleEvent(duration, name);
        }
        else {
            return new ScheduleEvent(duration, name, elements.get(2));
        }
    }

    @Override
    public String toString() {
        return settings == null ?
                String.format("ScheduleEvent %s [duration=%s]", name, duration) :
                String.format("ScheduleEvent %s [duration=%s settings=%s]", name, duration, settings);
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
