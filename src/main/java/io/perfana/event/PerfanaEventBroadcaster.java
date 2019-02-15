package io.perfana.event;

import io.perfana.client.api.TestContext;

public interface PerfanaEventBroadcaster {

    void broadcastBeforeTest(TestContext context, PerfanaEventProperties eventProperties);

    void broadcastAfterTest(TestContext context, PerfanaEventProperties eventProperties);

    void broadCastKeepAlive(TestContext context, PerfanaEventProperties eventProperties);

    void broadcastCustomEvent(TestContext context, PerfanaEventProperties eventProperties, ScheduleEvent event);
}
