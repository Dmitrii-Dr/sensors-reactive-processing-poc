package com.dmdr.sensors.broker;

import com.dmdr.sensors.message.Message;

public interface MessagePublisher extends AutoCloseable {

    void publish(Message message);

    @Override
    void close();
}
