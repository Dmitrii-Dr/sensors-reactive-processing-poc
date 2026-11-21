package com.dmdr.monitoring.alert;

import com.dmdr.monitoring.model.Measurement;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogNotificationSender implements NotificationSender {

    public void notify(Limit limit, Measurement measurement) {
        log.error("ALERT: {} sensorId={} value={} exceeds limit {} at={}",
                measurement.type(), measurement.sensorId(), measurement.value(), limit, measurement.dateTime());
    }
}
