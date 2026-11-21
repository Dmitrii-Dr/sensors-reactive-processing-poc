package com.dmdr.sensors.broker;

import com.dmdr.sensors.message.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class NatsMessagePublisher implements MessagePublisher<Message> {

    private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(5);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Connection connection;
    private final Scheduler scheduler = Schedulers.newSingle("nats-publisher");
    private final Sinks.Many<Message> queue = Sinks.many().unicast().onBackpressureBuffer();
    private final CompletableFuture<Void> finished;

    private NatsMessagePublisher(Connection connection, int queueCapacity) {
        this.connection = connection;
        this.finished = queue.asFlux()
                .onBackpressureBuffer(queueCapacity,
                        dropped -> log.warn("Publish queue full, dropping oldest {}", dropped),
                        BufferOverflowStrategy.DROP_OLDEST)
                .publishOn(scheduler)
                .doOnNext(this::send)
                .then()
                .toFuture();
    }

    public static NatsMessagePublisher connect(String url, int queueCapacity)
            throws IOException, InterruptedException {
        Options options = new Options.Builder()
                .server(url)
                .maxReconnects(-1)
                .build();
        Connection connection = Nats.connect(options);
        log.info("Connected to NATS at {}", url);
        return new NatsMessagePublisher(connection, queueCapacity);
    }

    @Override
    public void publish(Message message) {
        Sinks.EmitResult result;
        do {
            result = queue.tryEmitNext(message);
        } while (result == Sinks.EmitResult.FAIL_NON_SERIALIZED);

        if (result != Sinks.EmitResult.OK) {
            log.warn("Publish queue closed ({}), dropping {}", result, message);
        }
    }

    private void send(Message message) {
        try {
            byte[] payload = MAPPER.writeValueAsString(message).getBytes(StandardCharsets.UTF_8);
            connection.publish(message.getType().getTopic(), payload);
        } catch (JsonProcessingException | RuntimeException e) {
            log.error("Failed to publish {}", message, e);
        }
    }

    @Override
    public void close() {
        queue.tryEmitComplete();
        try {
            finished.get(CLOSE_TIMEOUT.toSeconds(), TimeUnit.SECONDS); // wait until the queue is flushed
            connection.drain(CLOSE_TIMEOUT).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Error while closing NATS publisher", e);
        } finally {
            scheduler.dispose();
        }
    }
}