package com.dmdr.sensors.measurement;

public class HumiditySensorMessage extends SensorMessage {
    // Additional unique fields may appear in the future.

    public HumiditySensorMessage(String sensorId, String value) {
        super(sensorId, value);
    }
}
