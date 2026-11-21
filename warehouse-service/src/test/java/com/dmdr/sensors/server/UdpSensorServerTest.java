package com.dmdr.sensors.server;

import com.dmdr.sensors.broker.MessagePublisher;
import com.dmdr.sensors.message.BrokerMessage;
import com.dmdr.sensors.message.MeasurementType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.netty.Connection;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UdpSensorServerTest {

    private static final int HUMIDITY_PORT = 3344;
    private static final int TEMPERATURE_PORT = 3355;

    private final BlockingQueue<BrokerMessage> received = new LinkedBlockingQueue<>();
    private final List<Connection> connections = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MessagePublisher<BrokerMessage> publisher = new MessagePublisher<>() {
            @Override
            public void publish(BrokerMessage message) {
                received.add(message);
            }

            @Override
            public void close() {
            }
        };
        connections.add(UdpSensorServer.start(HUMIDITY_PORT, MeasurementType.HUMIDITY, publisher)
                .block(Duration.ofSeconds(5)));
        connections.add(UdpSensorServer.start(TEMPERATURE_PORT, MeasurementType.TEMPERATURE, publisher)
                .block(Duration.ofSeconds(5)));
    }

    @AfterEach
    void tearDown() {
        connections.forEach(c -> {
            c.disposeNow();
        });
    }

    @Test
    void servesHumidityPort() throws Exception {
        send(HUMIDITY_PORT, "h1,45");

        BrokerMessage message = received.poll(5, TimeUnit.SECONDS);
        assertNotNull(message, "No message received for port " + HUMIDITY_PORT);
        assertEquals(MeasurementType.HUMIDITY, message.getType());
        assertEquals("h1", message.getSensorId());
        assertEquals("45", message.getValue());
    }

    @Test
    void servesTemperaturePort() throws Exception {
        send(TEMPERATURE_PORT, "t1,21");

        BrokerMessage message = received.poll(5, TimeUnit.SECONDS);
        assertNotNull(message, "No message received for port " + TEMPERATURE_PORT);
        assertEquals(MeasurementType.TEMPERATURE, message.getType());
        assertEquals("t1", message.getSensorId());
        assertEquals("21", message.getValue());
    }

    private static void send(int port, String payload) throws Exception {
        byte[] data = payload.getBytes(StandardCharsets.UTF_8);
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(new DatagramPacket(data, data.length, InetAddress.getLoopbackAddress(), port));
        }
    }
}
