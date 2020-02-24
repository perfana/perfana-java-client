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
package io.perfana.client;

import io.perfana.client.api.PerfanaConnectionSettingsBuilder;
import io.perfana.client.api.TestContextBuilder;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

public class PerfanaClientBuilderTest {

    @Test
    public void createWithAlternativeClass() {
         String alternativeClassCustomEvents = "  @generator-class=io.perfana.event.generator.EventScheduleGeneratorDefault \n" +
                 "  eventSchedule=PT1M|do-something \n";

         PerfanaClientBuilder perfanaClientBuilder = new PerfanaClientBuilder()
                 .setCustomEvents(alternativeClassCustomEvents)
                 .setTestContext(new TestContextBuilder().build())
                 .setPerfanaConnectionSettings(new PerfanaConnectionSettingsBuilder().build());

        PerfanaClient perfanaClient = perfanaClientBuilder.build(new URLClassLoader(new URL[]{}, Thread.currentThread().getContextClassLoader()));

        // TODO what to assert?

    }

}