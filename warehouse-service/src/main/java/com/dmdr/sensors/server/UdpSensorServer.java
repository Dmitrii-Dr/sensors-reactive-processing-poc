package com.dmdr.sensors.server;

import com.dmdr.sensors.broker.MessagePublisher;
import com.dmdr.sensors.measurement.SensorMessage;
import com.dmdr.sensors.measurement.SensorMessageParserUtils;
import com.dmdr.sensors.message.MeasurementType;
import com.dmdr.sensors.message.Message;
import com.dmdr.sensors.message.WarehouseSensorMessage;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.udp.UdpServer;

import java.time.Instant;
import java.util.function.BiFunction;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UdpSensorServer {

    public static Mono<? extends Connection> start(int port,
                                                   MeasurementType type,
                                                   MessagePublisher<Message> publisher) {
        return UdpServer.create()
                .host("0.0.0.0")
                .port(port)
                .handle((in, out) -> in.receiveObject()
                        .ofType(io.netty.channel.socket.DatagramPacket.class)
                        .doOnNext(packet -> handle(port, packet.content(), type, publisher))
                        .then())
                .bind();
    }

    private static void handle(int port, ByteBuf content, MeasurementType type, MessagePublisher<Message> publisher) {

        String text = content.toString(CharsetUtil.UTF_8).trim();
        SensorMessage sensorMessage = SensorMessageParserUtils.parse(text, type);
        if (sensorMessage == null) {
            log.error("[{}] Failed to parse {} message, dropping datagram: {}", port, type, text);
            return;
        }
        WarehouseSensorMessage message = new WarehouseSensorMessage(
                sensorMessage.getSensorId(), sensorMessage.getValue(), type, Instant.now());

        publisher.publish(message);

        log.debug("[{}] Queued for {}: deviceId={}, value={}",
                port, type.getTopic(), message.getDeviceId(), message.getValue());
    }
}
