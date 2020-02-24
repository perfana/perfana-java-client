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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store event properties per PerfanaEvent implementation class.
 * Uses getClass().getCanonicalName() so inner classes will use '.' instead of '$' as name separator.
 * '$' can not be used in most situation, like in xml element name (e.g. maven pom.xml).
 *
 * Intention is to be threadsafe using ConcurrentHashMaps, not 100% sure it is.
 */
public class PerfanaEventProperties {

    private Map<String, Map<String, String>> eventProperties = new ConcurrentHashMap<>();

    public EventProperties get(PerfanaEvent event) {
        String canonicalName = determineCanonicalName(event);
        Map<String, String> props = eventProperties.getOrDefault(canonicalName, Collections.emptyMap());
        return new EventProperties(props);
    }

    private static String determineCanonicalName(PerfanaEvent event) {
        String canonicalName = event.getClass().getCanonicalName();
        if (canonicalName == null) {
            String msg = String.format("Anonymous classes are not allowed for PerfanaEvent, sorry. [%s]", event.getClass());
            throw new PerfanaClientRuntimeException(msg);
        }
        return canonicalName;
    }

    public PerfanaEventProperties put(PerfanaEvent event, String name, String value) {
        String classImplName = determineCanonicalName(event);
        put(classImplName, name, value);
        return this;
    }

    public PerfanaEventProperties put(String eventClassImplName, String name, String value) {
        eventProperties.computeIfAbsent(eventClassImplName, k -> new ConcurrentHashMap<>()).put(name, value);
        return this;
    }

}
