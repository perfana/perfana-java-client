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
 * Adapter class with empty method implementations of the PerfanaEvent interface.
 * Extend this class so you only have to implement the methods that are used.
 *
 * Always provide a proper name for a PerfanaEvent for traceability.
 */
public abstract class PerfanaEventAdapter implements PerfanaEvent {

    @Override
    public void beforeTest(TestContext context, EventProperties properties) {

    }

    @Override
    public void afterTest(TestContext context, EventProperties properties) {

    }

    @Override
    public void keepAlive(TestContext context, EventProperties properties) {

    }

    @Override
    public void customEvent(TestContext context, EventProperties properties, ScheduleEvent scheduleEvent) {

    }
}
