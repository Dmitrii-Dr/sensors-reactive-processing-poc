package com.dmdr.sensors.server;

import com.dmdr.sensors.broker.MessagePublisher;
import com.dmdr.sensors.measurement.SensorMessage;
import com.dmdr.sensors.message.MeasurementType;
import com.dmdr.sensors.message.WarehouseSensorMessage;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.udp.UdpServer;

import java.time.Instant;
import java.util.function.BiFunction;

@Slf4j
public final class UdpSensorServer {

    private UdpSensorServer() {
    }

    /**
     * Binds a UDP server. Each datagram is expected as UTF-8 text "deviceId,value".
     * It is converted to a WarehouseSensorMessage and published to the topic of the given type.
     */
    public static Mono<? extends Connection> start(int port,
                                                   BiFunction<String, String, SensorMessage> factory,
                                                   MeasurementType type,
                                                   MessagePublisher publisher) {
        return UdpServer.create()
                .host("0.0.0.0")
                .port(port)
                .handle((in, out) -> in.receiveObject()
                        .ofType(io.netty.channel.socket.DatagramPacket.class)
                        .doOnNext(packet -> handle(port, packet.content(), factory, type, publisher))
                        .then())
                .bind();
    }

    private static void handle(int port, ByteBuf content, BiFunction<String, String, SensorMessage> factory,
                               MeasurementType type, MessagePublisher publisher) {
        String text = content.toString(CharsetUtil.UTF_8).trim();
        int idx = text.indexOf(',');
        if (idx < 0) {
            log.warn("[{}] Malformed datagram: {}", port, text);
            return;
        }
        SensorMessage sensorMessage = factory.apply(text.substring(0, idx).trim(), text.substring(idx + 1).trim());

        WarehouseSensorMessage message = new WarehouseSensorMessage(
                sensorMessage.getSensorId(), sensorMessage.getValue(), type, Instant.now());
        publisher.publish(message);
        log.debug("[{}] Queued for {}: deviceId={}, value={}",
                port, type.getTopic(), message.getDeviceId(), message.getValue());
    }
}
