# perfana-java-client

Add this java library to your project to easily integrate with Perfana.

# usage

Create a `PerfanaClient` using the builders:

```java
        PerfanaConnectionSettings settings = new PerfanaConnectionSettingsBuilder()
                .setPerfanaUrl("http://perfanaUrl")
                .setApiKey("perfana-api-key-XXX-YYY-ZZZ")
                .setRetryMaxCount("5")
                .setRetryTimeInSeconds("3")
                .build();

        TestContext context = new TestContextBuilder()
                .setSystemUnderTest("sut")
                .setWorkload("workload")
                .setTestEnvironment("env")
                .setTestRunId("testRunId")
                .setCIBuildResultsUrl("http://url")
                .setVersion("version")
                .setRampupTimeInSeconds("10")
                .setConstantLoadTimeInSeconds("300")
                .setAnnotations("annotation")
                .setVariables(new HashMap<>())
                .setTags(new ArrayList<>())
                .build();

        PerfanaClient client = new PerfanaClientBuilder()
                .setPerfanaConnectionSettings(settings)
                .setTestContext(context)
                .setAssertResultsEnabled(true)
                .setLogger(testLogger)
                .build();

```

Note that a lot of properties have decent defaults and do not need to be 
called, such as the retry and keep alive properties.

Then call these methods at the appropriate time:

### client.startSession()
Call when the load test starts. 

### client.stopSession()
Call when the load test stops. When assert results is enabled, 
a `PerfanaAssertionsAreFalse` will be thrown when the assert check 
is not ok.

## Use with events-*-maven-plugin

You can use the `perfana-java-client` as a plugin of the `events-*-maven-plugin` 
by putting the `perfana-java-client` jar on the classpath of the plugin.

You can use the `dependencies` element inside the `plugin` element.

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

You can substitute `event-scheduler-maven-plugin` by `event-gatling-maven-plugin`, `event-jmeter-maven-plugin`
and others when available.

Try this by calling:

    mvn -f src/test/resources/example-pom.xml event-scheduler:test

