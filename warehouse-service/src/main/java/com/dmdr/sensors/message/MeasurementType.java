package com.dmdr.sensors.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MeasurementType {
    TEMPERATURE("Temperature", "Celsius", "sensors.temperature"),
    HUMIDITY("Humidity", "Percent", "sensors.humidity");

    private final String typeName;
    private final String unitName;
    private final String topic;
}
