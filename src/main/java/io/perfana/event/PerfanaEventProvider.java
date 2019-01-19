package io.perfana.event;

import io.perfana.client.api.PerfanaTestContext;

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
    public void broadcastBeforeTest(PerfanaTestContext context, PerfanaEventProperties properties) {
        perfanaEventLoader.forEach(event -> event.beforeTest(context, properties.get(event)));
    }

    @Override
    public void broadcastAfterTest(PerfanaTestContext context, PerfanaEventProperties properties) {
        perfanaEventLoader.forEach(event -> event.afterTest(context, properties.get(event)));
    }
    
    @Override
    public void broadCastKeepAlive(PerfanaTestContext context, PerfanaEventProperties properties) {
        perfanaEventLoader.forEach(event -> event.keepAlive(context, properties.get(event)));

    }

    @Override
    public void broadcastCustomEvent(PerfanaTestContext context, PerfanaEventProperties properties, ScheduleEvent scheduleEvent) {
        perfanaEventLoader.forEach(event -> event.customEvent(context, properties.get(event), scheduleEvent));
    }


}
