# perfana-java-client

The Perfana Java Client can be used from Maven via an event-scheduler plugin, or it can be used
via the event-scheduler directly from code.

## Maven example

The following event-scheduler Maven plugins can be used:
* [event-scheduler-maven-plugin](https://github.com/perfana/event-scheduler-maven-plugin) runs plain event-scheduler via Maven 
* [event-gatling-maven-plugin](https://github.com/perfana/events-gatling-maven-plugin) runs Gatling load test via Maven with event-scheduler build-in
* [event-jmeter-maven-plugin](https://github.com/perfana/events-jmeter-maven-plugin)  runs jMeter load test via Maven with event-scheduler build-in

You can use the `perfana-java-client` as a plugin of the `events-*-maven-plugin`
by putting the `perfana-java-client` jar in the `dependencies` element 
of the Maven `plugin` element.

For example (from [example-pom.xml](src/test/resources/example-pom.xml)):

```xml
<plugin>
    <groupId>io.perfana</groupId>
    <artifactId>event-scheduler-maven-plugin</artifactId>
    <configuration>
        <eventSchedulerConfig>
            <debugEnabled>true</debugEnabled>
            <schedulerEnabled>true</schedulerEnabled>
            <failOnError>true</failOnError>
            <continueOnEventCheckFailure>true</continueOnEventCheckFailure>
            <scheduleScript>
                ${eventScheduleScript}
            </scheduleScript>
            <eventConfigs>
                <eventConfig implementation="io.perfana.event.PerfanaEventConfig">
                    <name>PerfanaEvent1</name>
                    <perfanaUrl>http://localhost:8888</perfanaUrl>
                    <apiKey>perfana-api-key-XXX-YYY-ZZZ</apiKey>
                    <assertResultsEnabled>false</assertResultsEnabled>
                    <variables>
                        <var1>my_value</var1>
                        <__var2>my_value_2</__var2>
                    </variables>
                    <testConfig>
                        <systemUnderTest>${systemUnderTest}</systemUnderTest>
                        <version>${version}</version>
                        <workload>${workload}</workload>
                        <testEnvironment>${testEnvironment}</testEnvironment>
                        <testRunId>${testRunId}</testRunId>
                        <buildResultsUrl>${buildResultsUrl}</buildResultsUrl>
                        <rampupTimeInSeconds>${rampupTimeInSeconds}</rampupTimeInSeconds>
                        <constantLoadTimeInSeconds>${constantLoadTimeInSeconds}</constantLoadTimeInSeconds>
                        <annotations>${annotations}</annotations>
                        <tags>${tags}</tags>
                    </testConfig>
                </eventConfig>
            </eventConfigs>
        </eventSchedulerConfig>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>io.perfana</groupId>
            <artifactId>perfana-java-client</artifactId>
            <version>${perfana-java-client.version}</version>
        </dependency>
    </dependencies>
</plugin>
```
Run via Maven:

    mvn -f src/test/resources/example-pom.xml event-scheduler:test

## code example 

The _Perfana Java Client_ used from code via the _Perfana Event Scheduler_.

Most properties have decent defaults and do not need to be
called, such as the retry and keep alive properties.

Minimal example:

```java
    EventLogger eventLogger = EventLoggerStdOut.INSTANCE;

    TestConfig testConfig = TestConfig.builder()
            .workload("testType")
            .testEnvironment("testEnv")
            .testRunId("testRunId")
            .buildResultsUrl("http://url")
            .version("version")
            .rampupTimeInSeconds(10)
            .constantLoadTimeInSeconds(300)
            .build();

    // enable the Perfana events
    PerfanaEventConfig perfanaEventConfig = new PerfanaEventConfig();
    perfanaEventConfig.setPerfanaUrl("http://localhost:8888");
    perfanaEventConfig.setApiKey("perfana-api-key-XXX-YYY-ZZZ");

    List<EventConfig> eventConfigs = new ArrayList<>();
    eventConfigs.add(perfanaEventConfig);

    EventSchedulerConfig eventSchedulerConfig = EventSchedulerConfig.builder()
            .testConfig(testConfig)
            .eventConfigs(eventConfigs)
            .build();

    EventScheduler scheduler = EventSchedulerBuilder.of(eventSchedulerConfig, eventLogger);

    scheduler.startSession();

    try {
        // instead of a sleep run or start a load test
        Thread.sleep(Duration.ofSeconds(20).toMillis());
    } finally {
        scheduler.stopSession();
    }
```

Example with more configuration settings and checks enabled:

```java
    EventLogger eventLogger = EventLoggerStdOut.INSTANCE;
    
    String scheduleScript1 =
    "PT1S|restart(restart to reset replicas)|{ 'server':'myserver' 'replicas':2, 'tags': [ 'first', 'second' ] }\n"
    + "PT10S|scale-down|{ 'replicas':1 }\n"
    + "PT18S|heapdump|server=myserver.example.com;port=1567";
    
    TestConfig testConfig = TestConfig.builder()
        .workload("testType")
        .testEnvironment("testEnv")
        .testRunId("testRunId")
        .buildResultsUrl("http://url-of-the-ci-build")
        .version("version")
        .rampupTimeInSeconds(10)
        .constantLoadTimeInSeconds(300)
        .annotations("annotation")
        .tags(Arrays.asList("tag1","tag2"))
        .build();
    
    // enable the Perfana events
    PerfanaEventConfig perfanaEventConfig = new PerfanaEventConfig();
    perfanaEventConfig.setPerfanaUrl("http://localhost:8888");
    perfanaEventConfig.setApiKey("perfana-api-key-XXX-YYY-ZZZ");
    perfanaEventConfig.setAssertResultsEnabled(true);
    Map<String, String> variables = Map.of("var1", "value1", "var2", "value2");
    perfanaEventConfig.setVariables(variables);
    
    List<EventConfig> eventConfigs = new ArrayList<>();
    eventConfigs.add(perfanaEventConfig);
    
    EventSchedulerConfig eventSchedulerConfig = EventSchedulerConfig.builder()
        .schedulerEnabled(true)
        .debugEnabled(false)
        .continueOnEventCheckFailure(false)
        .failOnError(true)
        .keepAliveIntervalInSeconds(120)
        .testConfig(testConfig)
        .eventConfigs(eventConfigs)
        .scheduleScript(scheduleScript1)
        .build();
    
    EventScheduler scheduler = EventSchedulerBuilder.of(eventSchedulerConfig, eventLogger);
    
    scheduler.startSession();
    
    try {
        // instead of a sleep run or start a load test
        Thread.sleep(Duration.ofSeconds(20).toMillis());
    } finally {
        scheduler.stopSession();
    }

    try {
        scheduler.checkResults();
    } catch (EventCheckFailureException e) {
        // deal with failed checks: e.g. fail the CI run
    }
```

Then call these methods at the appropriate time:

* `scheduler.startSession()` - at start of the load test 
* `scheduler.stopSession()` - at end of the load test
* `scheduler.checkResults()` - call to see if all checks of the test run are ok
* `scheduler.abortSession()` - call when the load test was aborted abnormally

The `checkResults()` throws `EventCheckFailureException` in case there are
events that report a failure.

You need the following dependencies:

```xml
<dependency>
    <groupId>io.perfana</groupId>
    <artifactId>event-scheduler</artifactId>
    <version>${event-scheduler.version}</version>
</dependency>
<dependency>
    <groupId>io.perfana</groupId>
    <artifactId>perfana-java-client</artifactId>
    <version>${perfana-java-client.version}</version>
</dependency>
```





