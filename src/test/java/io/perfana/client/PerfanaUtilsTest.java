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
package io.perfana.client;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class PerfanaUtilsTest {

    @Test
    public void splitAndTrim() {
        assertEquals(Collections.singletonList("one"), PerfanaUtils.splitAndTrim(" one ", ","));
        assertEquals(Arrays.asList("one", "two"), PerfanaUtils.splitAndTrim(" one,   two ", ","));
        assertEquals(Arrays.asList("o", ",", "t"), PerfanaUtils.splitAndTrim(" o , t", ""));
        assertEquals(Collections.emptyList(), PerfanaUtils.splitAndTrim(null, ","));
        assertEquals(Collections.emptyList(), PerfanaUtils.splitAndTrim(null, ""));
        assertEquals(Collections.emptyList(), PerfanaUtils.splitAndTrim(null, null));
    }

    @Test
    public void slashesIfNeeded() {
        assertEquals("/", PerfanaUtils.addSlashIfNeeded("", ""));
        assertEquals("base/end", PerfanaUtils.addSlashIfNeeded("base/", "end"));
        assertEquals("base/end", PerfanaUtils.addSlashIfNeeded("base/", "/end"));
        assertEquals("base/end", PerfanaUtils.addSlashIfNeeded("base", "/end"));
        assertEquals("base/end/", PerfanaUtils.addSlashIfNeeded("base//", "//end/"));
        assertEquals("/", PerfanaUtils.addSlashIfNeeded(null, ""));
        assertEquals("", PerfanaUtils.addSlashIfNeeded("", null));
        assertEquals("", PerfanaUtils.addSlashIfNeeded(null, null));
    }
}