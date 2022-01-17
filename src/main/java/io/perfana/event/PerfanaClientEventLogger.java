/*
 *    Copyright 2020-2022  Peter Paul Bakker @ Perfana.io, Daniel Moll @ Perfana.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.perfana.event;

import io.perfana.client.api.PerfanaClientLogger;
import io.perfana.eventscheduler.api.EventLogger;
import io.perfana.eventscheduler.log.EventLoggerStdOut;

public class PerfanaClientEventLogger implements PerfanaClientLogger {

    private final EventLogger eventLogger;

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
