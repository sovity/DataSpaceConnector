/*
 * Copyright 2020 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dataspaceconnector.telemetry.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.opentracing.noop.NoopTracer;

public class TelemetryConfigTest {
    private TelemetryConfig config = new TelemetryConfig();

    @Test
    public void jaegerTracer_is_NoopTracer() {
        /* ARRANGE */
        // Nothing to arrange here.

        /* ACT */
        final var result = config.jaegerTracer();

        /* ASSERT */
        assertTrue(result instanceof NoopTracer);
    }
}