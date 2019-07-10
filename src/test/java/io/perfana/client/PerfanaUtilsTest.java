package io.perfana.client;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

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
}