# Release notes Perfana Java Client

## 3.0.0 - April 2023

* use event-scheduler 4.0.0: no more TestConfig at Event level
* added init call to Perfana to fetch Perfana generated testRunId
  * avoids issues with reused testRunIds
  * if Perfana version with /init api call is present this will be used: if not the behavior is the same as before
  * if not present, testRunId must be set in TestConfig
  * use `<overrideTestRunId>true</overrideTestRunId>` to override the testRunId from Perfana
* fix message parsing for Perfana api benchmark 202 responses
* use java 11 instead of java 8

## 3.0.1 - January 2024

* change assertResultsEnabled to be true by default
* set default to use localhost:4000
* dependency updates, including event-scheduler 4.0.3
