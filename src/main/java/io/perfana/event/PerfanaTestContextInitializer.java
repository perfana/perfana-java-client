package io.perfana.event;

import io.perfana.client.PerfanaClient;
import io.perfana.client.PerfanaUtils;
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
            io.perfana.client.api.TestContext perfanaTestContext = PerfanaUtils.createPerfanaTestContext(perfanaEventContext);
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
