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

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Properties for event schedule generators.
 * Names that start with @ are filtered out.
 *
 *  This is an immutable class and makes an unmodifiable copies of the given Map.
 */
public class GeneratorProperties {
    private Map<String, String> properties;
    private Map<String, String> metaProperties;

    public GeneratorProperties(Map<String,String> props) {

        Map<String, String> propsMap = props.entrySet().stream()
                .filter(e -> !e.getKey().startsWith("@"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, String> metaMap = props.entrySet().stream()
                .filter(e -> e.getKey().startsWith("@"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        properties = Collections.unmodifiableMap(propsMap);
        metaProperties = Collections.unmodifiableMap(metaMap);
    }

    public GeneratorProperties() {
        properties = Collections.unmodifiableMap(Collections.emptyMap());
    }

    public GeneratorProperties(String propsAsText) {
        this(Collections.unmodifiableMap(createGeneratorSettings(propsAsText)));
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public String getMetaProperty(String name) {
        return metaProperties.get(name);
    }

    private static Map<String, String> createGeneratorSettings(String generatorSettingsAsText) {
        return Stream.of(generatorSettingsAsText.split("\n"))
                .map(line -> line.split("="))
                .filter(split -> split.length == 2)
                .filter(split -> split[0] != null && split[1] != null)
                .collect(Collectors.toMap(e -> e[0].trim(), e -> e[1].trim()));
    }

    @Override
    public String toString() {
        return "GeneratorProperties{" + "properties=" + properties +
                ", metaProperties=" + metaProperties +
                '}';
    }
}
