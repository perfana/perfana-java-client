package io.perfana.event2;

import io.perfana.client.api.PerfanaClientLogger;
import nl.stokpop.eventscheduler.api.EventLogger;
import nl.stokpop.eventscheduler.log.EventLoggerStdOut;

public class EventPerfanaClientLogger implements PerfanaClientLogger {

    private EventLogger eventLogger;

    public EventPerfanaClientLogger() {
        this(null);
    }

    public EventPerfanaClientLogger(EventLogger logger) {
        this.eventLogger = logger == null ? EventLoggerStdOut.INSTANCE : logger;
    }

    @Override
    public void info(String message) {
        this.eventLogger.info(message);
    }

    @Override
    public void warn(String message) {
        this.eventLogger.warn(message);
    }

    @Override
    public void error(String message) {
        this.eventLogger.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        this.eventLogger.error(message, throwable);
    }

    @Override
    public void debug(String message) {
        this.eventLogger.debug(message);
    }
}
