package com.dmdr.sensors.measurement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SensorMessage {
    // Common properties for all sensor types

    private final String sensorId;
    private final String value;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{sensorId=" + sensorId + ", value=" + value + "}";
    }
}
