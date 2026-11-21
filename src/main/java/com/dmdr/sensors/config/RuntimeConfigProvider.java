package com.dmdr.sensors.config;

public class RuntimeConfigProvider implements ConfigProvider {

    // Extension Point if Runtime Configuration is needed.
    @Override
    public ServerConfig getServerConfig() {
        //TODO read env var from docker context
        throw new UnsupportedOperationException("Runtime configuration is not supported yet");
    }
}
