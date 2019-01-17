package io.perfana.event;

import io.perfana.client.PerfanaClientRuntimeException;

import java.time.Duration;

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
        String[] elements = line.split("\\|");

        if (!(elements.length == 2 || elements.length == 3)) {
            throw new PerfanaClientRuntimeException("Wrong number of elements in line, expected 2 or 3 separated by '|': " + line);
        }
        
        Duration duration;
        try {
            duration = Duration.parse(elements[0]);
        } catch (Exception e) {
            throw new PerfanaClientRuntimeException("Failed to parse duration: " + elements[0] + " from line: " + line, e);
        }

        String name = elements[1];
        
        if (elements.length == 2) {
            return new ScheduleEvent(duration, name);
        }
        else {
            return new ScheduleEvent(duration, name, elements[2]);
        }
    }

    @Override
    public String toString() {
        return String.format("ScheduleEvent %s [duration=%s settings=%s]", name, duration, settings);
    }
}
