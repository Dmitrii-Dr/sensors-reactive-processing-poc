package com.dmdr.sensors.service;

import com.dmdr.sensors.measurement.SensorMessage;

public abstract class AbstractMessageProcessor implements MessageProcessor {

    private MessageProcessor next;

    @Override
    public MessageProcessor setNext(MessageProcessor next) {
        this.next = next;
        return next;
    }

    @Override
    public final void process(SensorMessage sensorMessage) {
        doProcess();
        if (next != null) {
            next.process(sensorMessage);
        }
    }

    protected abstract void doProcess();
}
