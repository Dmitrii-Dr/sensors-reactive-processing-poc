package com.dmdr.monitoring.store;

import com.dmdr.monitoring.model.Measurement;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MeasurementStore {

    private record Key(String type, String deviceId) {
    }

    private final Map<Key, Measurement> latest = new ConcurrentHashMap<>();

    public void put(Measurement value) {
        Key key = new Key(value.type(), value.sensorId());
        latest.merge(key, value,
                (old, cur) -> cur.dateTime() != null && old.dateTime() != null
                        && cur.dateTime().isBefore(old.dateTime()) ? old : cur);
    }

    public List<Measurement> snapshot() {
        return latest.values().stream()
                .sorted(Comparator.comparing(Measurement::type).thenComparing(Measurement::sensorId))
                .toList();
    }
}
