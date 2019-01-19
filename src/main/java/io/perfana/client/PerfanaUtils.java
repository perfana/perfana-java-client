package io.perfana.client;

public class PerfanaUtils {

    private PerfanaUtils() {}

    public static int parseInt(String variableName, String numberString, int defaultValue) {
        int time;
        try {
            time = Integer.parseInt(numberString);
        } catch (NumberFormatException e) {
            System.err.println(String.format("Unable to parse value of [%s=%s]: using default value [%d]. Error message: %s.", variableName, numberString, defaultValue, e.getMessage()));
            time = defaultValue;
        }
        return time;
    }

    public static boolean hasValue(String variable) {
        return variable != null && !variable.trim().isEmpty();
    }
}
