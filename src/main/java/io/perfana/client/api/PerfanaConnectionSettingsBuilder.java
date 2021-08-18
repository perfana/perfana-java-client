/*
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

import io.perfana.client.PerfanaUtils;

import java.time.Duration;

public class PerfanaConnectionSettingsBuilder {

    private static final int DEFAULT_RETRY_TIME_SECONDS = 10;
    private static final int DEFAULT_RETRY_MAX_COUNT = 30;
    private String perfanaUrl = "unknown";
    private int retryMaxCount = DEFAULT_RETRY_MAX_COUNT;
    private Duration retryDuration = Duration.ofSeconds(DEFAULT_RETRY_TIME_SECONDS);
    private String apiKey = null;

    public PerfanaConnectionSettingsBuilder setRetryMaxCount(String retryMaxCount) {
        this.retryMaxCount = PerfanaUtils.parseInt("retryMaxCount", retryMaxCount, DEFAULT_RETRY_MAX_COUNT);
        return this;
    }

    public PerfanaConnectionSettingsBuilder setRetryTimeInSeconds(String retryTimeInSeconds) {
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