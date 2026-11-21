package com.dmdr.sensors.server;

import com.dmdr.sensors.measurement.SensorMessage;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.udp.UdpServer;

import lombok.extern.slf4j.Slf4j;

import java.util.function.BiFunction;

@Slf4j
public final class UdpSensorServer {

    private UdpSensorServer() {
    }

    /**
     * Binds a UDP server. Each datagram is expected as UTF-8 text "sensorId,value".
     */
    public static Mono<? extends Connection> start(int port,
                                                   BiFunction<String, String, SensorMessage> factory) {
        return UdpServer.create()
                .port(port)
                .handle((in, out) -> in.receiveObject()
                        .ofType(io.netty.channel.socket.DatagramPacket.class)
                        .doOnNext(packet -> handle(port, packet.content(), factory))
                        .then())
                .bind();
    }

    private static void handle(int port, ByteBuf content, BiFunction<String, String, SensorMessage> factory) {
        String text = content.toString(CharsetUtil.UTF_8).trim();
        int idx = text.indexOf(',');
        if (idx < 0) {
            log.warn("[{}] Malformed datagram: {}", port, text);
            return;
        }
        SensorMessage message = factory.apply(text.substring(0, idx).trim(), text.substring(idx + 1).trim());
        log.debug("[{}] Received {}", port, message);
    }
}
