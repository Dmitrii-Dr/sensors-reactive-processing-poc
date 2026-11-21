package com.dmdr.monitoring.server;

import com.dmdr.monitoring.store.MeasurementStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.handler.codec.http.HttpHeaderNames;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

public final class MonitoringHttpServer {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private MonitoringHttpServer() {
    }

    public static Mono<? extends DisposableServer> start(int port, MeasurementStore store) {
        return HttpServer.create()
                .host("0.0.0.0")
                .port(port)
                .route(routes -> routes
                        .get("/health", (req, res) -> res.status(200).sendString(Mono.just("OK")))
                        .get("/measurements", (req, res) -> res
                                .header(HttpHeaderNames.CONTENT_TYPE, "application/json")
                                .sendString(Mono.fromCallable(() -> MAPPER.writeValueAsString(store.snapshot())))))
                .bind();
    }
}
