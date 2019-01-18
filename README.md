# perfana-java-client

Add this java library to your project to easily integrate with Perfana.

# usage

Create a PerfanaClient using the builder:

```java
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
                .setRetryTimeInSeconds("15")
                .setRetryMaxCount("10")
                .setKeepAliveTimeInSeconds("30")
                .createPerfanaClient();
```

Note that a lot of properties have decent defaults and do not need to be 
called, such as the retry and keep alive properties.

Then call these methods at the appropriate time:

### client.startSession()
Call when the load test starts. 

### client.stopSession()
Call when the load test stops. When assert results is enabled, 
a `PerfanaClientException` will be thrown when the assert check 
is not ok.

## Perfana Test Events

During a test run this Perfana Java Client emits events. You can put
your own implementation of the `PerfanaTestEvent` interface on the classpath
and add your own code to these events.

Events available, with example usage:
* _before test_ - use to restart servers or setup/cleanup environment
* _after test_ - start generating reports, clean up environment
* _keep alive calls_ - send calls to any remote API for instance
* _custom events_ - any event you can define in the event scheduler, e.g. failover, increase stub delay times or scale-down events 

The keep alive is scheduled each 15 seconds during the test.

The events will also be given a set of properties per implementation class.
The properties can be added before the test run using the `PerfanaClientBuilder`.

To add a property, use the addEventProperty when creating the client:

```java
builder.addEventProperty("nl.stokpop.MyEventClass", "name", "value")
```
	
## custom events

You can provide custom events via a list of <duration,eventName,eventSettings> tuples, 
one on each line.

The eventName can be any unique name among the custom events. You can use this eventName
in your own implementation of the PerfanaTestEvent interface to select what code to execute.

You can even send some specific settings to the event, using the eventSettings String.
Decide for your self if you want this to be just one value, a list of key-value pairs, 
json snippet or event base64 encoded contents.

Example:

```java
builder.setEventSchedule(eventSchedule)
```    
And as input:

```java
String eventSchedule =
        "PT5S|restart|{ server:'myserver' replicas:2 tags: [ 'first', 'second' ] }\n" +
        "PT10M|scale-down\n" +
        "PT10M45S|heapdump|server=myserver.example.com;port=1567\n" +
        "PT15M|scale-up|{ replicas:2 }\n";
```

Note the usage of ISO-8601 duration or period format, defined as PT(n)H(n)M(n)S.
Each period is from the start of the test, so not between events!

Above can be read as: 
* send restart event 5 seconds after start of test run. 
* send scale-down event 10 minutes after start of the test run.
* send heapdump event 10 minutes and 45 seconds after start of test run.
* send scale-up event 15 minutes after start of test run.

The setting will be send along with the event as well, for your own code to interpret.

When no settings are present, like with de scale-down event in this example, the settings
event will receive null for settings.

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
        <eventScheduleScript>
            PT5S|restart|{ server:'myserver' replicas:2 tags: [ 'first', 'second' ] }
            PT10M|scale-down
            PT10M45S|heapdump|server=myserver.example.com;port=1567
            PT15M|scale-up|{ replicas:2 }
        </eventScheduleScript>
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