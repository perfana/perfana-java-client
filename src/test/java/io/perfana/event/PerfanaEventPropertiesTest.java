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

        assertEquals(value, properties.get(new MyPerfanaEvent()).get(name));
    }

    private static final class MyPerfanaEvent extends PerfanaTestEventAdapter {
        // no implementation needed for this test
    }

    @Test
    public void nonExistingProperties() {
        PerfanaEventProperties properties = new PerfanaEventProperties();

        assertTrue(properties.get(new MyPerfanaEvent()).isEmpty());
    }

}