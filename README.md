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

## Perfana Test Events

During a test run this Perfana Java Client emits events. You can put
your own implementation of the `PerfanaTestEvent` interface on the classpath
and add your own code to these events.

Events available, with example usage:
* _before test_ - use to restart servers or setup/cleanup environment
* _keep alive calls_ - send calls to any remote API for instance
* _failover_ - initiate a failover during the test 
* _after test_ - start generating reports, clean up environment

The failover is scheduled 5 minutes after the end of the rampup.

The keep alive is scheduled each 15 seconds during the test.

The events will also be given a set of properties per implementation class.
The properties can be added before the test run using the `PerfanaClientBuilder`.

To add a property:
    
	builder.addEventProperty("nl.stokpop.MyEventClass", "name", "value")

## Use of events in maven plugin

To use the events via the Perfana Gatling maven plugin the jar with the
implementation details must be on the classpath of the plugin.

You can use the `dependencies` element inside the `plugin` element.

For example:

```xml 
<plugin>
    <groupId>io.perfana</groupId>
    <artifactId>perfana-gatling-maven-plugin</artifactId>
    <configuration>
        <perfanaEnabled>true</perfanaEnabled>
        <perfanaUrl>http://localhost:4000</perfanaUrl>
        <assertResultsEnabled>true</assertResultsEnabled>
        <simulationClass>afterburner.AfterburnerBasicSimulation</simulationClass>
        <perfanaEventProperties>
            <nl.stokpop.perfana.event.StokpopHelloPerfanaEvent>
                <myRestServer>https://my-rest-api</myName>
                <myCredentials>${ENV.SECRET}</myCredentials>
            </nl.stokpop.perfana.event.StokpopHelloPerfanaEvent>
        </perfanaEventProperties>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>nl.stokpop</groupId>
            <artifactId>perfana-hello-world-events</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
</plugin>
```   