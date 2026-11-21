package com.dmdr.sensors.config;

public class StaticConfigProvider implements ConfigProvider {
    private static final int HUMIDITY_PORT = 3344;
    private static final int TEMPERATURE_PORT = 3355;
    private static final int SHUTDOWN_TIMEOUT_SEC = 5;
    private static final String DEFAULT_NATS_URL = "nats://localhost:4222";
    private static final int PUBLISH_QUEUE_CAPACITY = 10_000;

    @Override
    public ServerConfig getServerConfig() {
        return new ServerConfig(HUMIDITY_PORT, TEMPERATURE_PORT, SHUTDOWN_TIMEOUT_SEC, DEFAULT_NATS_URL, PUBLISH_QUEUE_CAPACITY);
    }
}
