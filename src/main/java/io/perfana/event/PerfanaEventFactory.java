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
package io.perfana.event;

import io.perfana.eventscheduler.api.Event;
import io.perfana.eventscheduler.api.EventFactory;
import io.perfana.eventscheduler.api.EventLogger;
import io.perfana.eventscheduler.api.message.EventMessageBus;

public class PerfanaEventFactory implements EventFactory<PerfanaEventContext> {

    @Override
    public Event create(PerfanaEventContext context, EventMessageBus messageBus, EventLogger logger) {
        return new PerfanaEvent(context, messageBus, logger);
    }
}
