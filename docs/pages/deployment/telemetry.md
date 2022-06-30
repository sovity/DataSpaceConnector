---
layout: default
title: Telemetry
nav_order: 3
description: ""
permalink: /Deployment/Configuration/Telemetry
parent: Configuration
grand_parent: Deployment
---

# Telemetry
{: .fs-9 }

You want to have insights into a running Dataspace Connector? See what you have to do here.
{: .fs-6 .fw-300 }

---

To enable the OpenTelemetry collection via Jaeger export, modify the value in the
`application.properties` and add your Jeager telemetry collection endpoint:
```properties
opentelemetry.jaeger.endpoint=
```

The Dataspace Connector will now send OpenTelemetry data to the defined endpoint.

You can also run Jaeger besides the Dataspace Connector as Docker Container to view the collected OpenTelemetry traces and spans.
```
docker run -d --name jaeger \
  -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 \
  -p 5775:5775/udp \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 14268:14268 \
  -p 14250:14250 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.22
```
The traces can then be accessed at [http://localhost:16686](http://localhost:16686).
