package com.dmdr.sensors.measurement;

public class TemperatureSensorMessage extends SensorMessage {
    // Additional unique fields may appear in the future.

    public TemperatureSensorMessage(String sensorId, String value) {
        super(sensorId, value);
    }
}
