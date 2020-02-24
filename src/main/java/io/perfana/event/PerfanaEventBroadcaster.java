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

public interface PerfanaEventBroadcaster {

    void broadcastBeforeTest(TestContext context, PerfanaEventProperties eventProperties);

    void broadcastAfterTest(TestContext context, PerfanaEventProperties eventProperties);

    void broadCastKeepAlive(TestContext context, PerfanaEventProperties eventProperties);

    void broadcastCustomEvent(TestContext context, PerfanaEventProperties eventProperties, ScheduleEvent event);
}
