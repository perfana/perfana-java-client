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
