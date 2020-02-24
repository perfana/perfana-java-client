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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PerfanaEventPropertiesTest {

    @Test
    public void createEventProperties() {
        PerfanaEventProperties properties = new PerfanaEventProperties();

        String name = "my-name";
        String value = "my-value";
        properties.put("io.perfana.event.PerfanaEventPropertiesTest.MyPerfanaEvent", name, value);

        assertEquals(value, properties.get(new MyPerfanaEvent()).getProperty(name));
    }

    private static final class MyPerfanaEvent extends PerfanaEventAdapter {
        @Override
        public String getName() {
            return "MyPerfanaEvent";
        }
        // no further implementation needed for this test
    }

    @Test
    public void nonExistingProperties() {
        PerfanaEventProperties properties = new PerfanaEventProperties();

        assertTrue(properties.get(new MyPerfanaEvent()).isEmpty());
    }

}