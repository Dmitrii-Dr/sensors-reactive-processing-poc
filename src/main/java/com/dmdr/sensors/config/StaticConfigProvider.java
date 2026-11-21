package com.dmdr.sensors.config;

public class StaticConfigProvider implements ConfigProvider {
    private static final int HUMIDITY_PORT = 3344;
    private static final int TEMPERATURE_PORT = 3355;
    private static final int SHUTDOWN_TIMEOUT_SEC = 5;

    @Override
    public ServerConfig getServerConfig() {
        return new ServerConfig(HUMIDITY_PORT, TEMPERATURE_PORT, SHUTDOWN_TIMEOUT_SEC);
    }
}
