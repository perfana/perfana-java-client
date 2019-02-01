package io.perfana.event;

import io.perfana.client.PerfanaUtils;
import io.perfana.client.exception.PerfanaClientRuntimeException;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScheduleEvent {

    private static final Pattern nonAlphaNumsPattern = Pattern.compile("[^A-Za-z0-9\\- %\\+=]");
    
    private Duration duration;
    private String name;
    private String description;
    private String settings;

    public ScheduleEvent(Duration duration, String name, String description, String settings) {
        this.duration = duration;
        this.name = name;
        this.description = PerfanaUtils.hasValue(description) ? description : name + "-" + duration.toString();
        this.settings = settings;
    }

    public ScheduleEvent(Duration duration, String name, String description) {
        this(duration, name, description, null);
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
     * Use this format: duration|event-name(description)|settings
     * Note: description and settings are optional. duration is in ISO-8601 format.
     *
     * The duration is the time from the start of the test until the event to fire.
     *
     * Examples:
     * <il>
     *     <li>PT1M|change-backend-delay|delay=PT2S</li>
     *     <li>PT5M|change-backend-delay(set to extreme delay to test timeouts)|delay=PT10M</li>
     * </il>
     *
     * @param line line that is separated by duration|event-name(description)|settings
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
            throw new ScheduleEventWrongFormat("Wrong number of elements in line, expected 'duration|name(description)|setting' " +
                    "where (description) and settings are optional: [" + line + "]");
        }
        
        Duration duration;
        String textDuration = elements.get(0);
        String nameWithDescription = elements.get(1).trim();

        String[] nameAndDescriptionPair = extractNameAndDescription(nameWithDescription);
        String name = nameAndDescriptionPair[0];
        String description = nameAndDescriptionPair[1];

        try {
            duration = Duration.parse(textDuration);
        } catch (Exception e) {
            throw new ScheduleEventWrongFormat("Failed to parse duration: [" + textDuration + "] from line: [" + line + "]", e);
        }
        
        if (elements.size() == 2) {
            return new ScheduleEvent(duration, name, description);
        }
        else {
            String settings = elements.get(2);
            return new ScheduleEvent(duration, name, description, settings);
        }
    }

    /**
     * For name(description) return a String array of size 2 with { "name", "description" }
     *
     * For name returns { "name", "" }
     *
     * For name() returns { "name", "" }
     *
     * For (description) returns { "", "" }
     *
     * Also trims all values. And replaces all non alpha-numeric (and '+','-',' ','%') characters with _.
     *
     * @return array of size two with name and description (possibly empty string when not present)
     */
     static String[] extractNameAndDescription(String nameWithDescription) {
        if (!PerfanaUtils.hasValue(nameWithDescription)) return new String[] { "" , "" };
        if (!nameWithDescription.contains("(")) {
            return new String[] { nameWithDescription, "" };
        }
        int indexOpen = nameWithDescription.indexOf("(");
        int indexClose = nameWithDescription.lastIndexOf(")");
        if (indexClose == -1) { throw new PerfanaClientRuntimeException("closing parentheses ')' is missing"); }

        String name = nameWithDescription.substring(0, indexOpen).trim();
        String description = nameWithDescription.substring(indexOpen + 1, indexClose).trim();

        String sanatizedName = nonAlphaNumsPattern.matcher(name).replaceAll("_");
        String sanatizedDescription = nonAlphaNumsPattern.matcher(description).replaceAll("_");
        
        return new String[] { sanatizedName, sanatizedDescription };
    }

    @Override
    public String toString() {
        return settings == null ?
                String.format("ScheduleEvent %s [fire-at=%s]", name, duration) :
                String.format("ScheduleEvent %s [fire-at=%s settings=%s]", name, duration, settings);
    }

    public String getDescription() {
        return description;
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
