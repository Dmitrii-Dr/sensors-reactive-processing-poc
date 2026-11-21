package com.dmdr.sensors.service;

import com.dmdr.sensors.measurement.SensorMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DecodeProcessor extends AbstractMessageProcessor {
    @Override
    protected void doProcess(SensorMessage sensorMessage) {
        log.info("DecodeProcessor: processing");
    }
}
