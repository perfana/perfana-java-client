/*
 *    Copyright 2020-2021  Peter Paul Bakker @ Stokpop, Daniel Moll @ Perfana.io
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
package io.perfana.event.generator;

import io.perfana.eventscheduler.api.EventGenerator;
import io.perfana.eventscheduler.api.EventGeneratorFactory;
import io.perfana.eventscheduler.api.EventGeneratorProperties;
import io.perfana.eventscheduler.api.EventLogger;

public class PerfanaEventGeneratorFactory implements EventGeneratorFactory {

    @Override
    public EventGenerator create(EventGeneratorProperties properties, EventLogger logger) {
        throw new UnsupportedOperationException("Sorry, but the PerfanaEventGeneratorFactory has no implementation as of yet...");
    }
}
