package com.dmdr.monitoring.config;

import java.util.Optional;

public record MonitoringConfig(String natsUrl, String subject, int httpPort, int shutdownTimeoutSec) {

    private static final String DEFAULT_NATS_URL = "nats://localhost:4222";
    private static final String DEFAULT_SUBJECT = "sensors.>";
    private static final int DEFAULT_HTTP_PORT = 8080;
    private static final int DEFAULT_SHUTDOWN_TIMEOUT_SEC = 5;

    /** Reads settings from environment variables, falling back to defaults. */
    public static MonitoringConfig fromEnv() {
        return new MonitoringConfig(
                env("NATS_URL").orElse(DEFAULT_NATS_URL),
                env("NATS_SUBJECT").orElse(DEFAULT_SUBJECT),
                env("HTTP_PORT").map(Integer::parseInt).orElse(DEFAULT_HTTP_PORT),
                DEFAULT_SHUTDOWN_TIMEOUT_SEC);
    }

    private static Optional<String> env(String name) {
        return Optional.ofNullable(System.getenv(name)).filter(v -> !v.isBlank());
    }
}
