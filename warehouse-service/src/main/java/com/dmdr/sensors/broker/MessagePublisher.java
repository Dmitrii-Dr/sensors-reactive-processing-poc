package com.dmdr.sensors.broker;


/**
 * Publishes messages to a message broker.
 *
 * @param <T> the type of message to publish
 */
public interface MessagePublisher<T> extends AutoCloseable {

    /**
     * Publishes a message to the broker.
     *
     * @param message the message to publish
     */
    void publish(T message);

    /**
     * Releases any resources held by this publisher.
     */
    @Override
    void close();
}
