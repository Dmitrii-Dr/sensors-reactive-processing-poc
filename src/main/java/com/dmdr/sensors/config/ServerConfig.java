package com.dmdr.sensors.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServerConfig {
    private int humiditySensorPort;
    private int temperatureSensorPort;
    private int shutdownTimeoutSec;
}
