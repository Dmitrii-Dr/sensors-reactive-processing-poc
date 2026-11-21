package com.dmdr.sensors.broker;


import com.dmdr.sensors.message.Message;

public interface MessagePublisher<T> extends AutoCloseable {

    void publish(T message);

    @Override
    void close();
}
