package io.perfana.event.generator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GeneratorPropertiesTest {

    @Test
    public void retrieveProperties() {
        String text = "    @my-meta-prop    =     bar  \n" +
                "   my-prop    = foo   \n";

        GeneratorProperties props = new GeneratorProperties(text);

        // expect the properties to be visible in toString()
        assertTrue(props.toString().contains("bar"));
        assertTrue(props.toString().contains("foo"));
        
        assertEquals("bar", props.getMetaProperty("@my-meta-prop"));
        assertEquals("foo", props.getProperty("my-prop"));
        

    }

}