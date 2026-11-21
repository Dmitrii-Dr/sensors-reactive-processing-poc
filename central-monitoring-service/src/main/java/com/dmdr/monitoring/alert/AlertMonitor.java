package com.dmdr.monitoring.alert;

import com.dmdr.monitoring.model.Measurement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class AlertMonitor {
    private final AlertConfiguration configuration;

    public void verify(Measurement measurement) {
        Limit limit = configuration.getLimit(measurement.sensorId(), measurement.type());
        if (limit == null || measurement.value() == null) {
            return;
        }
        if (limitExceeded(limit, measurement.value())) {
            log.error("ALERT: {} sensorId={} value={} exceeds limit {} at={}",
                    measurement.type(), measurement.sensorId(), measurement.value(), limit, measurement.dateTime());
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
