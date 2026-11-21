package com.dmdr.sensors.message;

import java.time.Instant;

public class WarehouseSensorBrokerMessage extends BrokerMessage {
    // Additional unique fields may appear in the future.

    public WarehouseSensorBrokerMessage(String deviceId, String value, MeasurementType type, Instant dateTime) {
        super(deviceId, value, type, dateTime);
    }
}
