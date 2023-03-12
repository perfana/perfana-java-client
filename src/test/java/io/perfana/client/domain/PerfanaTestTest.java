/*
 *    Copyright 2020-2023  Peter Paul Bakker @ perfana.io, Daniel Moll @ perfana.io
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