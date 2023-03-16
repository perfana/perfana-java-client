/*
 *    Copyright 2020-2023  Peter Paul Bakker @ perfana.io, Daniel Moll @ perfana.io
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
package io.perfana.event;

import io.perfana.client.PerfanaClient;
import io.perfana.client.PerfanaUtils;
import io.perfana.client.api.PerfanaTestContext;
import io.perfana.eventscheduler.api.EventLogger;
import io.perfana.eventscheduler.api.TestContextInitializer;
import io.perfana.eventscheduler.api.config.TestContext;

public class PerfanaTestContextInitializer implements TestContextInitializer {

    private final PerfanaEventContext perfanaEventContext;
    private final EventLogger logger;

    PerfanaTestContextInitializer(PerfanaEventContext context, EventLogger logger) {
        this.perfanaEventContext = context;
        this.logger = logger;
    }

    @Override
    public TestContext extendTestContext(TestContext testContext) {
        if (!perfanaEventContext.isOverrideTestRunId()) {
            logger.info("Perfana test run id override is disabled. No override will be done.");
            return testContext;
        }
        else {
            PerfanaTestContext perfanaTestContext = PerfanaUtils.createPerfanaTestContext(perfanaEventContext, testContext);
            PerfanaClient perfanaClient = PerfanaUtils.createPerfanaClient(perfanaEventContext, perfanaTestContext, logger);
            String newTestRunId = perfanaClient.callInitTest(perfanaTestContext);
            if ("none".equals(newTestRunId)) {
                logger.warn("Perfana test run id is 'none'. No override will be done.");
                return testContext;
            }
            logger.info("Perfana test run id is '" + newTestRunId + "'. Will override test run id. ");
            return testContext.withTestRunId(newTestRunId);
        }
    }
}
