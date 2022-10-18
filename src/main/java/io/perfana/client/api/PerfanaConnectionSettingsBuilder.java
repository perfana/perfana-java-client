/*
 *    Copyright 2020-2022  Peter Paul Bakker @ Perfana.io, Daniel Moll @ Perfana.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.perfana.client.api;

import io.perfana.client.PerfanaUtils;

import java.time.Duration;

public class PerfanaConnectionSettingsBuilder {

    public static final int DEFAULT_RETRY_TIME_SECONDS = 6;
    public static final int DEFAULT_RETRY_MAX_COUNT = 30;
    private String perfanaUrl = "unknown";
    private int retryMaxCount = DEFAULT_RETRY_MAX_COUNT;
    private Duration retryDuration = Duration.ofSeconds(DEFAULT_RETRY_TIME_SECONDS);
    private String apiKey = null;

    public PerfanaConnectionSettingsBuilder setRetryMaxCount(String retryMaxCount) {
        this.retryMaxCount = PerfanaUtils.parseInt("retryMaxCount", retryMaxCount, DEFAULT_RETRY_MAX_COUNT);
        return this;
    }

    public PerfanaConnectionSettingsBuilder setRetryTimeSeconds(String retryTimeInSeconds) {
        this.retryDuration = Duration.ofSeconds(PerfanaUtils.parseInt("retryTimeInSeconds", retryTimeInSeconds, DEFAULT_RETRY_TIME_SECONDS));
        return this;
    }

    public PerfanaConnectionSettingsBuilder setRetryMaxCount(int retryMaxCount) {
        this.retryMaxCount = retryMaxCount;
        return this;
    }

    public PerfanaConnectionSettingsBuilder setRetryDuration(Duration retryDuration) {
        if (retryDuration != null) {
            this.retryDuration = retryDuration;
        }
        return this;
    }

    public PerfanaConnectionSettingsBuilder setPerfanaUrl(String perfanaUrl) {
        if (PerfanaUtils.hasValue(perfanaUrl)) {
            this.perfanaUrl = perfanaUrl;
        }
        return this;
    }

    public PerfanaConnectionSettingsBuilder setApiKey(String apiKey) {
        if (PerfanaUtils.hasValue(apiKey)) {
            this.apiKey = apiKey;
        }
        return this;
    }

    public PerfanaConnectionSettings build() {
        return new PerfanaConnectionSettings(retryMaxCount, retryDuration, perfanaUrl, apiKey);
    }

}