package com.dmdr.sensors.service;

import com.dmdr.sensors.config.ConfigProviderFactory;
import com.dmdr.sensors.config.RegisteredSensors;
import com.dmdr.sensors.measurement.SensorMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidationProcessor extends AbstractMessageProcessor {
    @Override
    protected void doProcess(SensorMessage sensorMessage) {
        log.info("ValidationProcessor: processing");
        String sensorId = sensorMessage.getSensorId();
        RegisteredSensors registeredSensors = ConfigProviderFactory.getInstance().getRegisteredSensors();
        if (!registeredSensors.getSensorIds().contains(sensorId)) {
            log.error("Received message from unknown sensor '{}': {}", sensorId, sensorMessage);
            throw new RuntimeException("Received message from unknown sensor");
        }
    }
}
