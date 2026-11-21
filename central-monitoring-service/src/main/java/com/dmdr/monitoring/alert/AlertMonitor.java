package com.dmdr.monitoring.alert;

import com.dmdr.monitoring.model.Measurement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class AlertMonitor {
    private final AlertConfiguration configuration;
    private final NotificationSender notificationSender;

    public void verify(Measurement measurement) {
        Limit limit = configuration.getLimit(measurement.sensorId(), measurement.type());
        if (limit == null || measurement.value() == null) {
            return;
        }
        if (limitExceeded(limit, measurement.value())) {
            notificationSender.notify(limit, measurement);
        }
    }

    private boolean limitExceeded(Limit limit, String value) {
        try {
            Double parsedValue = Double.valueOf(value);
            return limit.compareTo(parsedValue) < 0;
        } catch (NumberFormatException e) {
            log.warn("Cannot compare non-numeric value '{}' with limit {}", value, limit);
            return false;
        }
    }
}
