package com.dmdr.sensors.config;

public class EnvConfigProvider implements ConfigProvider {

    private final ServerConfig defaults = new DefaultConfigProvider().getServerConfig();

    @Override
    public ServerConfig getServerConfig() {
        return new ServerConfig(
                intEnv("HUMIDITY_PORT", defaults.getHumiditySensorPort()),
                intEnv("TEMPERATURE_PORT", defaults.getTemperatureSensorPort()),
                intEnv("SHUTDOWN_TIMEOUT_SEC", defaults.getShutdownTimeoutSec()),
                strEnv("NATS_URL", defaults.getNatsUrl()),
                intEnv("PUBLISH_QUEUE_CAPACITY", defaults.getQueueCapacity()));
    }

    private static String strEnv(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static int intEnv(String name, int fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : Integer.parseInt(value.trim());
    }
}
