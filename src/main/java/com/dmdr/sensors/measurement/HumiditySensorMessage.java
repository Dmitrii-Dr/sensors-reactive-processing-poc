package com.dmdr.sensors.measurement;

public class HumiditySensorMessage extends SensorMessage {
    // May have extra unique fields in the future

    public HumiditySensorMessage(String sensorId, String value) {
        super(sensorId, value);
    }
}
