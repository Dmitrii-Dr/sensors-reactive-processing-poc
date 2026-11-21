package com.dmdr.sensors.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Message {
    private final int deviceId;
    private final String value;
    private final MeasurementType type;
    private final Instant dateTime;
}
