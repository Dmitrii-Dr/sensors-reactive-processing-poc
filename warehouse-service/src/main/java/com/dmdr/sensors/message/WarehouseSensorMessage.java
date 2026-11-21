package com.dmdr.sensors.message;

import java.time.Instant;

public class WarehouseSensorMessage extends Message {
    // Additional unique fields may appear in the future.

    public WarehouseSensorMessage(String deviceId, String value, MeasurementType type, Instant dateTime) {
        super(deviceId, value, type, dateTime);
    }
}
