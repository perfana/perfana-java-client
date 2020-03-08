/*
 * Perfana Java Client - Java library that talks to the Perfana server
 * Copyright (C) 2020  Peter Paul Bakker @ Stokpop, Daniel Moll @ Perfana.io
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.perfana.event;

import io.perfana.client.api.PerfanaClientLogger;
import nl.stokpop.eventscheduler.api.EventLogger;
import nl.stokpop.eventscheduler.log.EventLoggerStdOut;

public class PerfanaClientEventLogger implements PerfanaClientLogger {

    private EventLogger eventLogger;

    public PerfanaClientEventLogger() {
        this(null);
    }

    public PerfanaClientEventLogger(EventLogger logger) {
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
