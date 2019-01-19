package io.perfana.client.api;

public interface PerfanaCaller {
    void callPerfanaEvent(PerfanaTestContext context, String eventDescription);
    void callPerfanaTestEndpoint(PerfanaTestContext context, boolean complete);
}
