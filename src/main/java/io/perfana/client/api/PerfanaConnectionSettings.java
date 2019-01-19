package io.perfana.client.api;

import java.time.Duration;

public class PerfanaConnectionSettings {

    private final int retryMaxCount;
    private final Duration retryDuration;
    private final Duration keepAliveDuration;
    private final String perfanaUrl;

    PerfanaConnectionSettings(int retryMaxCount, Duration retryDuration, Duration keepAliveDuration, String perfanaUrl) {
        this.retryMaxCount = retryMaxCount;
        this.retryDuration = retryDuration;
        this.keepAliveDuration = keepAliveDuration;
        this.perfanaUrl = perfanaUrl;
    }

    public int getRetryMaxCount() {
        return retryMaxCount;
    }

    public Duration getRetryDuration() {
        return retryDuration;
    }

    public Duration getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public String getPerfanaUrl() {
        return perfanaUrl;
    }

}
