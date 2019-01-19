package io.perfana.event;

import io.perfana.client.api.PerfanaTestContext;

public interface PerfanaEventBroadcaster {

    void broadcastBeforeTest(PerfanaTestContext context, PerfanaEventProperties eventProperties);

    void broadcastAfterTest(PerfanaTestContext context, PerfanaEventProperties eventProperties);

    void broadCastKeepAlive(PerfanaTestContext context, PerfanaEventProperties eventProperties);

    void broadcastCustomEvent(PerfanaTestContext context, PerfanaEventProperties eventProperties, ScheduleEvent event);
}
