package io.perfana.event;

import java.util.ServiceLoader;

public class PerfanaEventProvider implements PerfanaEventBroadcaster {

    private static PerfanaEventProvider eventProvider = new PerfanaEventProvider();

    private ServiceLoader<PerfanaTestEvent> perfanaEventLoader;

    private PerfanaEventProvider() {
        perfanaEventLoader = ServiceLoader.load(PerfanaTestEvent.class);
    }

    public static PerfanaEventProvider getInstance() {
        return eventProvider;
    }

    @Override
    public void broadcastBeforeTest(String testId, PerfanaEventProperties properties) {
        perfanaEventLoader.forEach(event -> event.beforeTest(testId, properties.get(event)));
    }

    @Override
    public void broadcastAfterTest(String testId, PerfanaEventProperties properties) {
        perfanaEventLoader.forEach(event -> event.afterTest(testId, properties.get(event)));
    }

    @Override
    public void broadcastFailover(String testId, PerfanaEventProperties properties) {
        perfanaEventLoader.forEach(event -> event.failover(testId, properties.get(event)));
    }

    @Override
    public void broadCastKeepAlive(final String testId, PerfanaEventProperties properties) {
        perfanaEventLoader.forEach(event -> event.keepAlive(testId, properties.get(event)));

    }


}
