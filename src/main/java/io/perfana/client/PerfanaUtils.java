package io.perfana.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PerfanaUtils {

    private PerfanaUtils() {}

    public static int parseInt(String variableName, String numberString, int defaultValue) {
        int time;
        try {
            time = Integer.parseInt(numberString);
        } catch (NumberFormatException e) {
            System.err.println(String.format("unable to parse value of [%s=%s]: using default value [%d]. Error message: %s.", variableName, numberString, defaultValue, e.getMessage()));
            time = defaultValue;
        }
        return time;
    }

    public static boolean hasValue(String variable) {
        return variable != null && !variable.trim().isEmpty();
    }

    public static int countOccurrences(String search, String text) {
        Matcher matcher = Pattern.compile(search).matcher(text);
        int count = 0;
        while (matcher.find()) { count = count + 1; }
        return count;
    }

    public static List<String> splitAndTrim(String text, String separator) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(text.split(separator))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
