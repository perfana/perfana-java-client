package io.perfana.service;

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
    public void broadcastBeforeTest(String testId) {
        perfanaEventLoader.forEach(event -> event.beforeTest(testId));
    }

    @Override
    public void broadcastAfterTest(String testId) {
        perfanaEventLoader.forEach(event -> event.afterTest(testId));
    }

    @Override
    public void broadcastFailover(String testId) {
        perfanaEventLoader.forEach(event -> event.failover(testId));
    }

    @Override
    public void broadCastKeepAlive(final String testId) {
        perfanaEventLoader.forEach(event -> event.keepAlive(testId));

    }


}
