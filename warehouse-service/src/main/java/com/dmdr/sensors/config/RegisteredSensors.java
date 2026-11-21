package com.dmdr.sensors.config;

import lombok.Data;

import java.util.Set;

@Data

public class RegisteredSensors {
    private final Set<String> sensorIds;
}
