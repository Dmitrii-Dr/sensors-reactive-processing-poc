package com.dmdr.sensors.measurement;

public class TemperatureSensorMessage extends SensorMessage {
    // May have extra unique fields in the future

    public TemperatureSensorMessage(String sensorId, String value) {
        super(sensorId, value);
    }
}
