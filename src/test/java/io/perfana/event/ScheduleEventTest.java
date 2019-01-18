package io.perfana.event;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScheduleEventTest {

    @Test(expected = ScheduleEvent.ScheduleEventWrongFormat.class)
    public void createFromLineNull() {
        ScheduleEvent event = ScheduleEvent.createFromLine(null);
    }

    @Test(expected = ScheduleEvent.ScheduleEventWrongFormat.class)
    public void createFromLineEmpty() {
        ScheduleEvent event = ScheduleEvent.createFromLine("");
    }

    @Test
    public void createFromLineWithSpaces() {
        ScheduleEvent event = ScheduleEvent.createFromLine("  PT13S|eventname|settings  =  0; foo= bar\n");

        assertEquals("eventname", event.getName());
        assertEquals(13, event.getDuration().getSeconds());
        assertEquals("settings  =  0; foo= bar", event.getSettings());
    }

}