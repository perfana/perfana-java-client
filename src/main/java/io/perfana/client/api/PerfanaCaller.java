package io.perfana.client.api;

public interface PerfanaCaller {
    void callPerfanaEvent(TestContext context, String eventDescription);
    void callPerfanaTestEndpoint(TestContext context, boolean complete);
}
