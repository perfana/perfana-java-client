package io.perfana.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Store event properties per PerfanaTestEvent implementation class.
 * Uses getClass().getCanonicalName() so innerclasses will use . instead of $ as name separator.
 * $ can not be used in most situation, like in xml element name (e.g. maven pom.xml).
 */
public class PerfanaEventProperties {
    private Map<String,Map<String,String>> eventProperties = new HashMap<>();

    public Map<String,String> get(PerfanaTestEvent event) {
        return eventProperties.getOrDefault(event.getClass().getCanonicalName(), Collections.emptyMap());
    }

    public PerfanaEventProperties put(PerfanaTestEvent event, String name, String value) {
        String classImplName = event.getClass().getCanonicalName();
        put(classImplName, name, value);
        return this;
    }

    public PerfanaEventProperties put(String eventClassImplName, String name, String value) {
        if (eventProperties.containsKey(eventClassImplName)) {
            eventProperties.get(eventClassImplName).put(name, value);
        }
        else {
            Map<String,String> properties = new HashMap<>();
            properties.put(name, value);
            eventProperties.put(eventClassImplName, properties);
        }
        return this;
    }

}
