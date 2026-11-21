package com.dmdr.monitoring.broker;

import com.dmdr.monitoring.model.Measurement;
import com.dmdr.monitoring.store.MeasurementStore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;

@Slf4j
public final class NatsMeasurementSubscriber implements AutoCloseable {

    private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(5);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final Connection connection;
    private final MeasurementStore store;

    private NatsMeasurementSubscriber(Connection connection, MeasurementStore store) {
        this.connection = connection;
        this.store = store;
    }

    public static NatsMeasurementSubscriber start(String url, String subject, MeasurementStore store)
            throws IOException, InterruptedException {
        Connection connection = Nats.connect(new Options.Builder()
                .server(url)
                .maxReconnects(-1)
                .build());
        log.info("Connected to NATS at {}", url);

        NatsMeasurementSubscriber subscriber = new NatsMeasurementSubscriber(connection, store);
        Dispatcher dispatcher = connection.createDispatcher(subscriber::onMessage);
        dispatcher.subscribe(subject);
        log.info("Subscribed to '{}'", subject);
        return subscriber;
    }

    private void onMessage(Message message) {
        try {
            Measurement measurement = MAPPER.readValue(message.getData(), Measurement.class);
            store.put(measurement);
            log.info("[{}] deviceId={}, value={}, at={}", message.getSubject(),
                    measurement.deviceId(), measurement.value(), measurement.dateTime());
        } catch (IOException | RuntimeException e) {
            log.warn("Skipping malformed message on '{}'", message.getSubject(), e);
        }
    }

    @Override
    public void close() {
        try {
            connection.drain(CLOSE_TIMEOUT).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Error while closing NATS subscriber", e);
        }
    }
}
