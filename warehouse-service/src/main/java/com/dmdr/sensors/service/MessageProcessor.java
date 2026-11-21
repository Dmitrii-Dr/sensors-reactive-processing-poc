package com.dmdr.sensors.service;

import com.dmdr.sensors.measurement.SensorMessage;

/**
 * Processes sensor messages as a step in a chain of processors.
 */
public interface MessageProcessor {

    /**
     * Sets the next processor in the chain.
     *
     * @param next the processor to run after this one
     * @return the given next processor, to allow chaining
     */
    MessageProcessor setNext(MessageProcessor next);

    /**
     * Processes a sensor message.
     *
     * @param sensorMessage the message to process
     */
    void process(SensorMessage sensorMessage);
}
