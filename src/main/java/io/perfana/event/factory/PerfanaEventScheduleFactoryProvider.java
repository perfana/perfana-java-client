package io.perfana.event.factory;

import io.perfana.client.api.PerfanaClientLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class PerfanaEventScheduleFactoryProvider {

    private Map<String, PerfanaEventScheduleFactory> factories;
    private PerfanaClientLogger logger;

    PerfanaEventScheduleFactoryProvider(Map<String, PerfanaEventScheduleFactory> factories, PerfanaClientLogger logger) {
        this.factories = Collections.unmodifiableMap(new HashMap<>(factories));
        this.logger = logger;
    }

    public static PerfanaEventScheduleFactoryProvider createInstanceFromClasspath(PerfanaClientLogger logger) {
        ServiceLoader<PerfanaEventScheduleFactory> perfanaEventFactoryLoader = ServiceLoader.load(PerfanaEventScheduleFactory.class);
        // java 9+: List<PerfanaTestEvent> factories = perfanaEventLoader.stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
        Map<String, PerfanaEventScheduleFactory> factories = new HashMap<>();
        for (PerfanaEventScheduleFactory factory : perfanaEventFactoryLoader) {
            factories.put(factory.getClass().getName(), factory);
        }
        return new PerfanaEventScheduleFactoryProvider(factories, logger);
    }

    public PerfanaEventScheduleFactory find(String factoryClassname) {
        return factories.get(factoryClassname);
    }
}
