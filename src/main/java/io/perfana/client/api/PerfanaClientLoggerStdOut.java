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
package io.perfana.client.api;

/**
 * Logs to standard out for convenience. Add your own logger preferably where possible.
 */
public class PerfanaClientLoggerStdOut implements PerfanaClientLogger {

        @Override
        public void info(final String message) {
            say("INFO ", message);
        }

        @Override
        public void warn(final String message) {
            say("WARN ", message);
        }

        @Override
        public void error(final String message) {
            say("ERROR", message);
        }

        @Override
        public void error(final String message, Throwable throwable) {
            say("ERROR", message, throwable);
        }

        @Override
        public void debug(final String message) {
            say("DEBUG", message);
        }

        private void say(String level, String something) {
            System.out.printf("## %s ## %s%n", level, something);
        }
        private void say(String level, String something, Throwable throwable) {
            System.out.printf("## %s ## %s %s: %s%n", level, something, throwable.getClass().getName(), throwable.getMessage());
            throwable.printStackTrace();
        }
}
