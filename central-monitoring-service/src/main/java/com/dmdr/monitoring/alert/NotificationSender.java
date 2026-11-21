package com.dmdr.monitoring.alert;

import com.dmdr.monitoring.model.Measurement;

public interface NotificationSender {
    void notify(Limit limit, Measurement measurement);
}
