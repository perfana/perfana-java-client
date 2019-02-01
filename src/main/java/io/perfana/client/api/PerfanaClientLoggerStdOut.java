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
