package io.perfana.service;

public interface PerfanaEventBroadcaster {

    void broadcastBeforeTest(String testId);

    void broadcastAfterTest(String testId);

    void broadcastFailover(String testId);

    void broadCastKeepAlive(String testd);
}
