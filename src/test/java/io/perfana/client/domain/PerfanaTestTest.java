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
package io.perfana.client.domain;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

public class PerfanaTestTest {

    public static final String PERFANA_TEST_JSON = "perfana-test.json";
    private static final ObjectReader perfanaTestReader;
    private static final ObjectWriter perfanaTestWriter;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        perfanaTestReader = objectMapper.reader().forType(PerfanaTest.class);
        perfanaTestWriter = objectMapper.writer().forType(PerfanaTest.class);
    }

    @Test
    public void testReadJson() throws IOException {
        InputStream testJson = PerfanaTestTest.class.getClassLoader().getResourceAsStream(PERFANA_TEST_JSON);
        if (testJson == null) { throw new IOException(PERFANA_TEST_JSON + " not found"); }
        PerfanaTest perfanaMessage = perfanaTestReader.readValue(testJson);
        String jsonTest = perfanaTestWriter.writeValueAsString(perfanaMessage);
        System.out.println(jsonTest);
        assertNotNull(jsonTest);
    }

}