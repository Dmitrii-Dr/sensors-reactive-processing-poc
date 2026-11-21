package com.dmdr.sensors.broker;


public interface MessagePublisher<T> extends AutoCloseable {

    void publish(T message);

    @Override
    void close();
}
