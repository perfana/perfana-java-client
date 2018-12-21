package io.perfana.client;

public class PerfanaClientRuntimeException extends RuntimeException {

    PerfanaClientRuntimeException(final String message) {
        super(message);
    }

    PerfanaClientRuntimeException(final String message, final Exception e) {
        super(message, e);
    }
}
