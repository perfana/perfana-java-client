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