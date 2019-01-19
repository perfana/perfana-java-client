package io.perfana.event;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ScheduleEventTest {

    @Test(expected = ScheduleEvent.ScheduleEventWrongFormat.class)
    public void createFromLineNull() {
        ScheduleEvent.createFromLine(null);
    }

    @Test(expected = ScheduleEvent.ScheduleEventWrongFormat.class)
    public void createFromLineEmpty() {
        ScheduleEvent.createFromLine(" ");
    }

    @Test(expected = ScheduleEvent.ScheduleEventWrongFormat.class)
    public void createFromLineDurationOnly() {
        ScheduleEvent.createFromLine("  PT0S ");
    }

    @Test(expected = ScheduleEvent.ScheduleEventWrongFormat.class)
    public void createFromLineInvalidDuration() {
        ScheduleEvent.createFromLine("PT0X|name");
    }

    @Test(expected = ScheduleEvent.ScheduleEventWrongFormat.class)
    public void createFromLineInvalidDurationEmpty() {
        ScheduleEvent.createFromLine(" |name");
    }

    @Test(expected = ScheduleEvent.ScheduleEventWrongFormat.class)
    public void createFromLineInvalidTooMany() {
        ScheduleEvent.createFromLine("z|name|y|x");
    }

    @Test
    public void createFromLineWithSpaces() {
        ScheduleEvent event = ScheduleEvent.createFromLine("  PT13S|eventname|settings  =  0; foo= bar\n");

        assertEquals("eventname", event.getName());
        assertEquals(13, event.getDuration().getSeconds());
        assertEquals("settings  =  0; foo= bar", event.getSettings());
    }

    @Test
    public void createFromLineWithNoSettings() {
        ScheduleEvent event = ScheduleEvent.createFromLine("PT13S|eventname\n");

        assertEquals("eventname", event.getName());
        assertEquals(13, event.getDuration().getSeconds());
        assertNull(event.getSettings());
    }

}