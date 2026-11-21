package com.dmdr.sensors.server;

import com.dmdr.sensors.broker.MessagePublisher;
import com.dmdr.sensors.message.BrokerMessage;
import com.dmdr.sensors.message.MeasurementType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import reactor.netty.Connection;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class UdpSensorServerPublishTest {

    private MessagePublisher<BrokerMessage> publisher;
    private Connection connection;
    private int port;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws Exception {
        publisher = mock(MessagePublisher.class);
        try (DatagramSocket s = new DatagramSocket(0)) {
            port = s.getLocalPort();
        }
        connection = UdpSensorServer.start(port, MeasurementType.TEMPERATURE, publisher)
                .block(Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() {
        if (connection != null) {
            connection.disposeNow();
        }
    }

    @Test
    void publishesMessageWhenFormatIsCorrect() throws Exception {
        send("t1,21");

        ArgumentCaptor<BrokerMessage> captor = ArgumentCaptor.forClass(BrokerMessage.class);
        verify(publisher, timeout(5000)).publish(captor.capture());
        BrokerMessage message = captor.getValue();
        assertEquals("t1", message.getSensorId());
        assertEquals("21", message.getValue());
        assertEquals(MeasurementType.TEMPERATURE, message.getType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "garbage", "only-id,", "a,b,c", ","})
    void doesNotPublishWhenEmptyOrWrongFormat(String payload) throws Exception {
        send(payload);

        verify(publisher, after(500).never()).publish(any());
    }

    @Test
    void wrongDatagramDoesNotBlockSubsequentValidOne() throws Exception {
        send("garbage");
        send("t1,30");

        verify(publisher, timeout(5000)).publish(any());
        verify(publisher, after(300).times(1)).publish(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown-sensor,21", "sensor-t1,21", "T1,21"})
    void doesNotPublishMessageFromUnknownSensor(String payload) throws Exception {
        send(payload);

        verify(publisher, after(500).never()).publish(any());
    }

    @Test
    void unknownSensorDoesNotBlockSubsequentKnownOne() throws Exception {
        send("unknown-sensor,21");
        send("t1,30");

        ArgumentCaptor<BrokerMessage> captor = ArgumentCaptor.forClass(BrokerMessage.class);
        verify(publisher, timeout(5000)).publish(captor.capture());
        assertEquals("t1", captor.getValue().getSensorId());
        verify(publisher, after(300).times(1)).publish(any());
    }

    private void send(String payload) throws Exception {
        byte[] data = payload.getBytes(StandardCharsets.UTF_8);
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(new DatagramPacket(data, data.length, InetAddress.getLoopbackAddress(), port));
        }
    }
}
