package io.perfana.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This is an immutable class and makes an unmodifiable copy of the given Map.
 */
public class EventProperties {
    private Map<String, String> properties;

    public EventProperties(Map<String,String> props) {
        properties = Collections.unmodifiableMap(new HashMap<>(props));
    }

    public EventProperties() {
        properties = Collections.unmodifiableMap(Collections.emptyMap());
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public String getPropertyOrDefault(String name, String defaultValue) {
        return properties.getOrDefault(name, defaultValue);
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public String toString() {
        return "EventProperties{" +
                "properties=" + properties +
                '}';
    }

}
