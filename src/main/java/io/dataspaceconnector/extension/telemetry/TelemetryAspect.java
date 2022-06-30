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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Logic of the AOP aspect for OpenTelemetry.
 */
@Aspect
@Component
public class TelemetryAspect {
    /**
     * Defines the logic of the aspect annotation.
     *
     * @param joinPoint Context information to which the annotation was applied.
     * @return Continuation statement for the annotated interrupted method.
     * @throws Throwable If the method cannot be executed further.
     */
    @Around("@annotation(TelemetrySpan)")
    @SuppressFBWarnings("THROWS_METHOD_THROWS_CLAUSE_THROWABLE")
    public Object addSpan(final ProceedingJoinPoint joinPoint) throws Throwable {
        //Get context information of annotated method.
        final var signature = (MethodSignature) joinPoint.getSignature();
        final var method = signature.getMethod();
        final var telemetrySpan = method.getAnnotation(TelemetrySpan.class);

        //Span name: Either the name-parameter specified by the user or
        //automatically the class name + method name.
        final var spanName = telemetrySpan.name().isBlank()
                ? signature.getDeclaringTypeName().substring(
                        signature.getDeclaringTypeName()
                                .lastIndexOf('.') + 1).trim() + "." + method.getName()
                : telemetrySpan.name();


        //Create span for annotated method
        final var span = CustomOpenTelemetry.getTracer().spanBuilder(spanName).startSpan();
        span.makeCurrent();
        final var proceeded = joinPoint.proceed();
        span.end();

        return proceeded;
    }
}
