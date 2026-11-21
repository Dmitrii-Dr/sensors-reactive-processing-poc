package com.dmdr.sensors.config;

/**
 * Provides access to the application configuration.
 */
public interface ConfigProvider {

    /**
     * Returns the server configuration.
     *
     * @return the server config
     */
    ServerConfig getServerConfig();

    /**
     * Returns the sensors registered in the configuration.
     *
     * @return the registered sensors
     */
    RegisteredSensors getRegisteredSensors();
}
