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
package io.perfana.client.api;

import java.util.Map;

public interface PerfanaCaller {
    void callPerfanaEvent(TestContext context, String eventTitle, String eventDescription);
    void callPerfanaTestEndpoint(TestContext context, boolean complete);
    void callPerfanaTestEndpoint(TestContext context, boolean complete, Map<String,String> extraVariables);

    /**
     * Call before test starts to get a unique test run id.
     * @param context the test context
     * @return a unique test run id to be used in the test
     */
    String callInitTest(TestContext context);
}
