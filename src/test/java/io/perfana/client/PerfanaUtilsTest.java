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