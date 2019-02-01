package io.perfana.event;

import io.perfana.client.exception.PerfanaClientRuntimeException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
        ScheduleEvent event = ScheduleEvent.createFromLine("  PT13S|eventname( )|settings  =  0; foo= bar\n");

        assertEquals("eventname", event.getName());
        assertEquals(13, event.getDuration().getSeconds());
        assertEquals("eventname-PT13S", event.getDescription());
        assertEquals("settings  =  0; foo= bar", event.getSettings());
    }

    @Test
    public void createFromLineWithNoSettings() {
        ScheduleEvent event = ScheduleEvent.createFromLine("PT13S|eventname\n");

        assertEquals("eventname", event.getName());
        assertEquals(13, event.getDuration().getSeconds());
        assertEquals("eventname-PT13S", event.getDescription());
        assertNull(event.getSettings());
    }

    @Test
    public void createFromLineWithEventDescription() {
        ScheduleEvent event = ScheduleEvent.createFromLine("PT13S|eventname(Nice description of event)\n");

        assertEquals("eventname", event.getName());
        assertEquals(13, event.getDuration().getSeconds());
        assertEquals("Nice description of event", event.getDescription());
        assertNull(event.getSettings());
    }

    @Test
    public void createFromLineNastyCharacters() {
        String nastyJavascript = "javascript: alert(document.cookie);";
        ScheduleEvent event = ScheduleEvent.createFromLine("  PT13S|eventname(" + nastyJavascript + ")|settings  =  0; foo= bar\n");

        assertEquals("eventname", event.getName());
        assertEquals(13, event.getDuration().getSeconds());
        assertNotEquals(nastyJavascript, event.getDescription());
        assertEquals("settings  =  0; foo= bar", event.getSettings());
    }

    @Test
    public void extractNameAndDescription1() {
        String[] nameAndDescription = ScheduleEvent.extractNameAndDescription("");
        assertEquals(2, nameAndDescription.length);
        assertEquals("", nameAndDescription[0]);
        assertEquals("", nameAndDescription[1]);
    }

    @Test
    public void extractNameAndDescription2() {
        String[] nameAndDescription = ScheduleEvent.extractNameAndDescription("my-name(my-description)");
        assertEquals(2, nameAndDescription.length);
        assertEquals("my-name", nameAndDescription[0]);
        assertEquals("my-description", nameAndDescription[1]);
    }

    @Test
    public void extractNameAndDescription3() {
        String[] nameAndDescription = ScheduleEvent.extractNameAndDescription("   my-name    (  my-description = +100%   )   ");
        assertEquals(2, nameAndDescription.length);
        assertEquals("my-name", nameAndDescription[0]);
        assertEquals("my-description = +100%", nameAndDescription[1]);
    }

    @Test
    public void extractNameAndDescription4() {
        String[] nameAndDescription = ScheduleEvent.extractNameAndDescription("      (    )   ");
        assertEquals(2, nameAndDescription.length);
        assertEquals("", nameAndDescription[0]);
        assertEquals("", nameAndDescription[1]);
    }

    @Test
    public void extractNameAndDescription5() {
        String nastyJavascript = "javascript: alert(document.cookie);";
        String[] nameAndDescription = ScheduleEvent.extractNameAndDescription("  nastyJavascript     (  " + nastyJavascript + "  )   ");
        assertEquals(2, nameAndDescription.length);
        assertEquals("nastyJavascript", nameAndDescription[0]);
        assertNotEquals(nastyJavascript, nameAndDescription[1]);
    }

    @Test(expected = PerfanaClientRuntimeException.class)
    public void extractNameAndDescription6() {
        ScheduleEvent.extractNameAndDescription("my-name( x  ");
    }


}