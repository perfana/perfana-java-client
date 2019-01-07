package io.perfana.event;

public interface PerfanaEventBroadcaster {

    void broadcastBeforeTest(String testId, PerfanaEventProperties eventProperties);

    void broadcastAfterTest(String testId, PerfanaEventProperties eventProperties);

    void broadcastFailover(String testId, PerfanaEventProperties eventProperties);

    void broadCastKeepAlive(String testd, PerfanaEventProperties eventProperties);
}
