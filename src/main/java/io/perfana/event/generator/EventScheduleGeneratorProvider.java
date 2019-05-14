package io.perfana.event.generator;

import io.perfana.client.api.PerfanaClientLogger;
import io.perfana.event.EventScheduleGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class EventScheduleGeneratorProvider {

    private Map<String, EventScheduleGenerator> generators;
    private PerfanaClientLogger logger;

    EventScheduleGeneratorProvider(Map<String, EventScheduleGenerator> generators, PerfanaClientLogger logger) {
        this.generators = Collections.unmodifiableMap(new HashMap<>(generators));
        this.logger = logger;
    }

    public static EventScheduleGeneratorProvider createInstanceFromClasspath(PerfanaClientLogger logger) {
        return createInstanceFromClasspath(logger, null);
    }

    public static EventScheduleGeneratorProvider createInstanceFromClasspath(PerfanaClientLogger logger, ClassLoader classLoader) {
        ServiceLoader<EventScheduleGenerator> generatorLoader = classLoader == null
                ? ServiceLoader.load(EventScheduleGenerator.class)
                : ServiceLoader.load(EventScheduleGenerator.class, classLoader);
        // java 9+: List<PerfanaEvent> generators = perfanaEventLoader.stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
        Map<String, EventScheduleGenerator> generators = new HashMap<>();
        for (EventScheduleGenerator generator : generatorLoader) {
            String generatorName = generator.getClass().getName();
            logger.info("registering EventScheduleGenerator: " + generatorName);
            generators.put(generatorName, generator);
        }
        return new EventScheduleGeneratorProvider(generators, logger);
    }

    public EventScheduleGenerator find(String generatorClassname) {
        return generators.get(generatorClassname);
    }
}
