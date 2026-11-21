package com.dmdr.monitoring.alert;

import com.dmdr.monitoring.model.Measurement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AlertMonitorTest {

    private static final Limit LIMIT = new Limit(35);

    private AlertConfiguration configuration;
    private NotificationSender sender;
    private AlertMonitor monitor;

    @BeforeEach
    void setUp() {
        configuration = mock(AlertConfiguration.class);
        sender = mock(NotificationSender.class);
        monitor = new AlertMonitor(configuration, sender);
        when(configuration.getLimit("s1", "temperature")).thenReturn(LIMIT);
    }

    @Test
    void notifiesWhenLimitExceeded() {
        Measurement measurement = measurement("36");

        monitor.verify(measurement);

        verify(sender).notify(LIMIT, measurement);
    }

    @Test
    void doesNotNotifyWhenBelowLimit() {
        monitor.verify(measurement("20"));

        verifyNoInteractions(sender);
    }

    @Test
    void doesNotNotifyWhenEqualToLimit() {
        monitor.verify(measurement("35"));

        verifyNoInteractions(sender);
    }

    @Test
    void doesNotNotifyWhenNoLimitConfigured() {
        when(configuration.getLimit("s1", "temperature")).thenReturn(null);

        monitor.verify(measurement("100"));

        verify(sender, never()).notify(any(), any());
    }

    @Test
    void doesNotNotifyWhenValueIsNullOrNotNumeric() {
        monitor.verify(measurement(null));
        monitor.verify(measurement("abc"));

        verifyNoInteractions(sender);
    }

    private static Measurement measurement(String value) {
        return new Measurement("s1", value, "temperature", Instant.now());
    }
}
