package com.dmdr.sensors.server;

import com.dmdr.sensors.broker.MessagePublisher;
import com.dmdr.sensors.measurement.SensorMessage;
import com.dmdr.sensors.measurement.SensorMessageParserUtils;
import com.dmdr.sensors.message.MeasurementType;
import com.dmdr.sensors.message.BrokerMessage;
import com.dmdr.sensors.message.WarehouseSensorBrokerMessage;
import com.dmdr.sensors.service.MessageProcessorProvider;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.Connection;
import reactor.netty.udp.UdpServer;

import java.time.Instant;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UdpSensorServer {

    private static final Scheduler PARSER_SCHEDULER =
            Schedulers.newParallel("sensor-parser", Runtime.getRuntime().availableProcessors());

    public static Mono<? extends Connection> start(int port,
                                                   MeasurementType type,
                                                   MessagePublisher<BrokerMessage> publisher) {
        return UdpServer.create()
                .host("0.0.0.0")
                .port(port)
                .handle((in, out) -> in.receiveObject()
                        .ofType(DatagramPacket.class)
                        .map(packet -> packet.content().toString(CharsetUtil.UTF_8))
                        .onBackpressureDrop(dropped ->
                                log.warn("[{}] Parser is overloaded, dropping datagram", port))
                        .publishOn(PARSER_SCHEDULER)
                        .doOnNext(text -> handle(port, text, type, publisher))
                        .then())
                .bind();
    }

    private static void handle(int port, String rawText, MeasurementType type, MessagePublisher<BrokerMessage> publisher) {
        try {
            doHandle(port, rawText, type, publisher);
        } catch (RuntimeException e) {
            log.error("[{}] Unexpected error while handling datagram, dropping: {}", port, rawText, e);
        }
    }

    private static void doHandle(int port, String rawText, MeasurementType type, MessagePublisher<BrokerMessage> publisher) {
        String text = rawText.trim();
        SensorMessage sensorMessage = SensorMessageParserUtils.parse(text, type);
        if (sensorMessage == null) {
            log.error("[{}] Failed to parse {} message, dropping datagram: {}", port, type, text);
            return;
        }
        //Dummy call of chain processing for future needs
        try {
            MessageProcessorProvider.getChain().process(sensorMessage);
        } catch (RuntimeException e) {
            log.error("[{}] Message rejected by processing chain, dropping: {} ({})",
                    port, sensorMessage, e.getMessage());
            return;
        }

        BrokerMessage brokerMessage = new WarehouseSensorBrokerMessage(
                sensorMessage.getSensorId(), sensorMessage.getValue(), type, Instant.now());

        publisher.publish(brokerMessage);

        log.debug("[{}] Queued for {}: sensorId={}, value={}",
                port, type.getTopic(), brokerMessage.getSensorId(), brokerMessage.getValue());
    }
}
