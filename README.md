# perfana-java-client

Add this java library to your project to easily integrate with Perfana.

# usage

Create a PerfanaClient using the builder:

```
PerfanaClient client =
        new PerfanaClientBuilder()
                .setApplication("application")
                .setTestType("testType")
                .setTestEnvironment("testEnv")
                .setTestRunId("testRunId")
                .setCIBuildResultsUrl("url")
                .setApplicationRelease("release")
                .setRampupTimeInSeconds("10")
                .setConstantLoadTimeInSeconds("60")
                .setPerfanaUrl("perfUrl")
                .setAnnotations("annotations")
                .setVariables(new Properties())
                .setAssertResultsEnabled(true)
                .createPerfanaClient();

```

Then call these methods when appropriate:

### client.startSession()
Call when the load test starts. 

### client.stopSession()
Call when the load test stops. When assert results is enabled, 
a PerfanaClientException will be thrown when the assert check 
is not ok.

