package io.perfana.client;

public class PerfanaClientException extends Exception {
    
    PerfanaClientException(final String message) {
        super(message);
    }

    PerfanaClientException(final String message, final Exception e) {
        super(message, e);
    }
}
