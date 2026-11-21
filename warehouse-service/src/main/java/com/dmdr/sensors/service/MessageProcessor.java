package com.dmdr.sensors.service;

import com.dmdr.sensors.measurement.SensorMessage;

public interface MessageProcessor {

    MessageProcessor setNext(MessageProcessor next);

    void process(SensorMessage sensorMessage);
}
