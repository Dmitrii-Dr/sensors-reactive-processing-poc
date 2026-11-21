package com.dmdr.sensors.message;

import java.time.Instant;

public class WarehouseSensorBrokerMessage extends BrokerMessage {
    // Additional unique fields may appear in the future.

    public WarehouseSensorBrokerMessage(String sensorId, String value, MeasurementType type, Instant dateTime) {
        super(sensorId, value, type, dateTime);
    }
}
