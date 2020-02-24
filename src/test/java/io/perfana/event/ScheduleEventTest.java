/**
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