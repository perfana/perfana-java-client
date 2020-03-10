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
package io.perfana.client;

import io.perfana.client.api.PerfanaClientLogger;
import io.perfana.client.api.PerfanaClientLoggerStdOut;
import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.TestContext;
import io.perfana.client.exception.PerfanaClientRuntimeException;

public class PerfanaClientBuilder {

    private TestContext testContext;

    private PerfanaConnectionSettings perfanaConnectionSettings;

    private boolean assertResultsEnabled = false;

    private PerfanaClientLogger logger = new PerfanaClientLoggerStdOut();

    public PerfanaClientBuilder setTestContext(TestContext context) {
        this.testContext = context;
        return this;
    }

    public PerfanaClientBuilder setLogger(PerfanaClientLogger logger) {
        this.logger = logger;
        return this;
    }

    public PerfanaClientBuilder setPerfanaConnectionSettings(PerfanaConnectionSettings settings) {
        this.perfanaConnectionSettings = settings;
        return this;
    }

    public PerfanaClientBuilder setAssertResultsEnabled(boolean assertResultsEnabled) {
        this.assertResultsEnabled = assertResultsEnabled;
        return this;
    }

    /**
     * Create PerfanaClient.
     *
     * @return a new PerfanaClient
     */
    public PerfanaClient build() {

        if (testContext == null) {
            throw new PerfanaClientRuntimeException("TestContext must be set, it is null.");
        }

        if (perfanaConnectionSettings == null) {
            throw new PerfanaClientRuntimeException("PerfanaConnectionSettings must be set, it is null.");
        }

        return new PerfanaClient(testContext, perfanaConnectionSettings, assertResultsEnabled, logger);
    }

}