package io.perfana.client.api;

public interface PerfanaClientLogger {

    void info(String message);
    void warn(String message);
    void error(String message);
    void debug(String message);

}
