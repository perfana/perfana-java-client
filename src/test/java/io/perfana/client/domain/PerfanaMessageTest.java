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