package com.dmdr.sensors.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageProtocol {

    /** Position of the device id in the datagram. */
    public static final int SENSOR_ID_INDEX = 0;

    /** Position of the measured value in the datagram. */
    public static final int VALUE_INDEX = 1;

    /** Number of values a valid datagram must contain. */
    public static final int VALUES_COUNT = 2;

    public static char getSpliterator() {
        return ',';
    }
}
