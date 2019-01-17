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
    public void broadCastKeepAlive(String testId, PerfanaEventProperties properties) {
        perfanaEventLoader.forEach(event -> event.keepAlive(testId, properties.get(event)));

    }

    @Override
    public void broadcastCustomEvent(String testId, PerfanaEventProperties properties, ScheduleEvent scheduleEvent) {
        perfanaEventLoader.forEach(event -> event.customEvent(testId, properties.get(event), scheduleEvent));
    }


}
