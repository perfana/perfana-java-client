/*
 *    Copyright 2020-2021  Peter Paul Bakker @ Stokpop, Daniel Moll @ Perfana.io
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class PerfanaMessageTest {

    public static final String PERFANA_MESSAGE_JSON = "perfana-message.json";
    private static final ObjectReader perfanaMessageReader;
    private static final ObjectWriter perfanaMessageWriter;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        perfanaMessageReader = objectMapper.reader().forType(PerfanaMessage.class);
        perfanaMessageWriter = objectMapper.writer().forType(PerfanaMessage.class);
    }

    private static final Pattern CI_BUILD_PATTERN = Pattern.compile("cibuildresultsurl");

    @Test
    public void testReadJson() throws IOException {
        InputStream testJson = PerfanaMessageTest.class.getClassLoader().getResourceAsStream(PERFANA_MESSAGE_JSON);
        if (testJson == null) { throw new IOException(PERFANA_MESSAGE_JSON + " not found"); }
        PerfanaMessage perfanaMessage = perfanaMessageReader.readValue(testJson);
        String jsonTest = perfanaMessageWriter.writeValueAsString(perfanaMessage);
        //System.out.println(jsonTest);
        // with some camelcase naming there can be two field with different cases in the json
        // so, @JsonProperty("CIBuildResultsUrl")
        //    private final String cibuildResultsUrl;
        assertEquals(1, findMatches(jsonTest.toLowerCase()));
    }

    @Test
    public void testWriteJson() throws IOException {
        DeepLink deepLink1 = DeepLink.builder()
            .url("https://loadrunner-cloud.saas.microfocus.com/run-overview/2/dashboard/?TENANTID=411235333&projectId=1")
            .name("loadrunner-cloud-dashboard")
            .type("dashboard")
            .pluginName("test-events-loadrunner-cloud")
            .build();

        DeepLink deepLink2 = DeepLink.builder()
            .url("https://my-wiremock.example.com:9999/__admin/recorder/")
            .name("my-wiremock recorder")
            .type("wiremock")
            .pluginName("test-events-wiremock")
            .build();

        PerfanaMessage perfanaMessage = PerfanaMessage.builder()
            .cibuildResultsUrl("http://test-url")
            .deepLink(deepLink1)
            .deepLink(deepLink2)
            .build();

        String jsonTest = perfanaMessageWriter.writeValueAsString(perfanaMessage);
        //System.out.println(jsonTest);
        assertEquals(1, findMatches(jsonTest.toLowerCase()));
    }

    private int findMatches(String text) {
        Matcher matcher = CI_BUILD_PATTERN.matcher(text);
        int count = 0;
        while (matcher.find()) { count++; }
        return count;
    }

}