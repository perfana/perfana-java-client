/**
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

import io.perfana.client.api.TestContext;

/**
 * This interface can be implemented in other jars and be put on the classpath.
 *
 * Provide a file in META-INF/services/io.perfana.event.PerfanaEvent that contains the
 * fully qualified name of the implementing class.
 *
 * This class will be used when these events are called. Possibly you can even provide multiple implementations
 * on the classpath that will all be called.
 *
 * For more information on how this technically works, check out javadoc of java.util.ServiceLoader.
 */
public interface PerfanaEvent {

    /**
     * @return name of the test event.
     */
    String getName();

    /**
     * Called before the test run starts. You can for instance cleanup the test environment and/or
     * restart the server under test.
     * @param context the test run context
     * @param properties e.g. REST_URL="https://my-rest-url"
     */
    void beforeTest(TestContext context, EventProperties properties);

    /**
     * Called after the test run is done. Use for instance to start creating a report of some sort or
     * remove the test environment.
     * @param context the test run context
     * @param properties e.g. REST_URL="https://my-rest-url"
     */
    void afterTest(TestContext context, EventProperties properties);
    
    /**
     * Called for each keep alive event for this test run.
     * @param context the test run context
     * @param properties e.g. REST_URL="https://my-rest-url"
     */
    void keepAlive(TestContext context, EventProperties properties);

    /**
     * Called for each custom event, according to the custom even schedule.
     * @param context the test run context
     * @param properties e.g. REST_URL="https://my-rest-url"
     * @param scheduleEvent the custom event, use to execute specific behaviour in the event handler
     */
    void customEvent(TestContext context, EventProperties properties, ScheduleEvent scheduleEvent);
    
}
