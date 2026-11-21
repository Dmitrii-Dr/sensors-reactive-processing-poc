package com.dmdr.sensors.config;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EnvConfigProvider implements ConfigProvider {

    private final ServerConfig serverConfig;

    public EnvConfigProvider() {
        ServerConfig defaults = new DefaultConfigProvider().getServerConfig();
        this.serverConfig = new ServerConfig(
                intEnv("HUMIDITY_PORT", defaults.getHumiditySensorPort()),
                intEnv("TEMPERATURE_PORT", defaults.getTemperatureSensorPort()),
                intEnv("SHUTDOWN_TIMEOUT_SEC", defaults.getShutdownTimeoutSec()),
                strEnv("NATS_URL", defaults.getNatsUrl()),
                intEnv("PUBLISH_QUEUE_CAPACITY", defaults.getQueueCapacity()));
    }

    @Override
    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    @Override
    public RegisteredSensors getRegisteredSensors() {
        String value = System.getenv("SENSOR_IDS");
        if (value == null || value.isBlank()) {
            return new DefaultConfigProvider().getRegisteredSensors();
        }
        Set<String> ids = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        return new RegisteredSensors(ids);
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
