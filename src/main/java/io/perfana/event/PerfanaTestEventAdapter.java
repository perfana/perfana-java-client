package io.perfana.event;

import java.util.Map;

/**
 * Adapter class with empty method implementations of the PerfanaTestEvent interface.
 * Extend this class so you only have to implement the methods that are used.
 */
public abstract class PerfanaTestEventAdapter implements PerfanaTestEvent {

    @Override
    public void beforeTest(final String testId, final Map<String, String> eventProperties) {

    }

    @Override
    public void afterTest(final String testId, final Map<String, String> eventProperties) {

    }

    @Override
    public void failover(final String testId, final Map<String, String> eventProperties) {

    }

    @Override
    public void keepAlive(final String testId, final Map<String, String> eventProperties) {

    }
}
