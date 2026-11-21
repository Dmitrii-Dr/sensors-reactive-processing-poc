package com.dmdr.sensors;

import com.dmdr.sensors.broker.MessagePublisher;
import com.dmdr.sensors.broker.NatsMessagePublisher;
import com.dmdr.sensors.config.ConfigProvider;
import com.dmdr.sensors.config.ServerConfig;
import com.dmdr.sensors.config.StaticConfigProvider;
import com.dmdr.sensors.measurement.HumiditySensorMessage;
import com.dmdr.sensors.measurement.TemperatureSensorMessage;
import com.dmdr.sensors.message.MeasurementType;
import com.dmdr.sensors.server.UdpSensorServer;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

import java.time.Duration;

@Slf4j
public class Main {



    public static void main(String[] args) throws Exception {
        ConfigProvider configProvider = new StaticConfigProvider();
        ServerConfig serverConfig = configProvider.getServerConfig();

        MessagePublisher publisher = NatsMessagePublisher.connect(
                serverConfig.getNatsUrl(), serverConfig.getQueueCapacity());

        Mono.zip(
                        UdpSensorServer.start(serverConfig.getHumiditySensorPort(),
                                HumiditySensorMessage::new, MeasurementType.HUMIDITY, publisher),
                        UdpSensorServer.start(serverConfig.getTemperatureSensorPort(),
                                TemperatureSensorMessage::new, MeasurementType.TEMPERATURE, publisher))
                .doOnNext(servers -> {
                    registerShutdownHook(serverConfig, publisher, servers.getT1(), servers.getT2());
                    log.info("UDP servers listening: humidity={}, temperature={}",
                            serverConfig.getHumiditySensorPort(), serverConfig.getTemperatureSensorPort());
                })
                .flatMap(servers -> Mono.when(servers.getT1().onDispose(), servers.getT2().onDispose()))
                .doOnError(e -> log.error("Failed to start UDP servers", e))
                .doFinally(signal -> publisher.close())
                .block(); // single block only keeps main alive; both binds run in parallel
    }

    private static void registerShutdownHook(ServerConfig serverConfig, MessagePublisher publisher,
                                             Connection... connections) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down UDP servers...");
            for (Connection connection : connections) {
                try {
                    connection.disposeNow(Duration.ofSeconds(serverConfig.getShutdownTimeoutSec()));
                } catch (RuntimeException e) {
                    log.warn("Error while disposing connection", e);
                }
            }
            publisher.close();
            log.info("Shutdown complete");
        }, "udp-shutdown-hook"));
    }
}
