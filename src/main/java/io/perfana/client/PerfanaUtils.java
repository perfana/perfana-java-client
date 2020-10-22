/*
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
            System.err.printf("unable to parse value of [%s=%s]: using default value [%d]. Error message: %s.%n", variableName, numberString, defaultValue, e.getMessage());
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
