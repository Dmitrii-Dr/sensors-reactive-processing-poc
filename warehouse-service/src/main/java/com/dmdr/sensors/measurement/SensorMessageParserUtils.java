package com.dmdr.sensors.measurement;

import com.dmdr.sensors.message.MeasurementType;
import com.dmdr.sensors.message.MessageProtocol;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class SensorMessageParserUtils {

    public static SensorMessage parse(String text, MeasurementType type) {
        String[] values = text.split(String.valueOf(MessageProtocol.getSpliterator()));
        if (values.length != MessageProtocol.VALUES_COUNT) {
            log.warn("[{}] Malformed datagram: {}", type, text);
            return null;
        }
        String sensorId = values[MessageProtocol.SENSOR_ID_INDEX];
        String value = values[MessageProtocol.VALUE_INDEX];

        return switch (type) {
            case HUMIDITY -> new HumiditySensorMessage(sensorId, value);
            case TEMPERATURE -> new TemperatureSensorMessage(sensorId, value);
        };
    }
}
