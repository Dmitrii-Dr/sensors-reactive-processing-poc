package com.dmdr.monitoring;

import com.dmdr.monitoring.alert.AlertConfiguration;
import com.dmdr.monitoring.alert.AlertMonitor;
import com.dmdr.monitoring.broker.NatsMeasurementSubscriber;
import com.dmdr.monitoring.config.MonitoringConfig;
import com.dmdr.monitoring.server.MonitoringHttpServer;
import com.dmdr.monitoring.store.MeasurementStore;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.DisposableServer;

import java.time.Duration;

@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {
        MonitoringConfig config = MonitoringConfig.fromEnv();
        MeasurementStore store = new MeasurementStore();

        AlertMonitor alertMonitor = new AlertMonitor(new AlertConfiguration());

        NatsMeasurementSubscriber subscriber =
                NatsMeasurementSubscriber.start(config.natsUrl(), config.subject(), store, alertMonitor);
        DisposableServer server = MonitoringHttpServer.start(config.httpPort(), store).block();
        log.info("HTTP server listening on port {}", config.httpPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            server.disposeNow(Duration.ofSeconds(config.shutdownTimeoutSec()));
            subscriber.close();
            log.info("Shutdown complete");
        }, "monitoring-shutdown-hook"));

        server.onDispose().block();
    }
}
