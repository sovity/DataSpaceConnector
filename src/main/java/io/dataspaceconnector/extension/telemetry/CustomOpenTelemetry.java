/*
 * Copyright 2020-2022 sovity GmbH
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
package io.dataspaceconnector.extension.telemetry;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Initializes and builds an OpenTelemetry SDK with a Jaeger exporter.
 */
@Configuration
public class CustomOpenTelemetry {
    /**
     * Timeout for Jaeger call.
     */
    public static final int TIMEOUT = 30;

    /**
     * Initialized {@link Tracer}.
     */
    @Getter
    private static Tracer tracer;

    /**
     * CustomOpenTelemetry constructor, initializes the {@link Tracer}.
     * @param jeagerEndpoint The Jeager endpoint.
     */
    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public CustomOpenTelemetry(
            @Value("${opentelemetry.jaeger.endpoint:}") final String jeagerEndpoint) {
        final var openTelemetry = initOpenTelemetry(jeagerEndpoint);
        tracer = openTelemetry.getTracer("io.dataspaceconnector.extension.telemetry.OpenTelemetry");
    }

    /**
     * Initialize an OpenTelemetry SDK with a Jaeger exporter.
     *
     * @param jeagerEndpoint The Jeager endpoint.
     * @return A ready-to-use {@link OpenTelemetry} instance.
     */
    private OpenTelemetry initOpenTelemetry(final String jeagerEndpoint) {
        SdkTracerProvider tracerProvider;

        if (jeagerEndpoint.isBlank()) {
            tracerProvider = SdkTracerProvider.builder().build();
        } else {
            tracerProvider = getTracerProvider(jeagerEndpoint);
        }

        final var openTelemetry =
                OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();

        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));

        return openTelemetry;
    }

    @NotNull
    private SdkTracerProvider getTracerProvider(final String jeagerEndpoint) {
        SdkTracerProvider tracerProvider;
        final var serviceNameResource =
                Resource.create(
                        Attributes.of(ResourceAttributes.SERVICE_NAME, "dataspace-connector"));

        final var jaegerExporter = JaegerGrpcSpanExporter.builder()
                            .setEndpoint(jeagerEndpoint)
                            .setTimeout(TIMEOUT, TimeUnit.SECONDS)
                            .build();

        tracerProvider = SdkTracerProvider.builder()
                            .addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter))
                            .setResource(Resource.getDefault().merge(serviceNameResource))
                            .build();

        return tracerProvider;
    }
}
