package com.dmdr.monitoring.alert;


public class AlertConfiguration {
    private static final double DEFAULT_TEMPERATURE_LIMIT = 35;
    private static final double DEFAULT_HUMIDITY_LIMIT = 50;

    private final Limit temperatureLimit = new Limit(env("TEMPERATURE_LIMIT", DEFAULT_TEMPERATURE_LIMIT));
    private final Limit humidityLimit = new Limit(env("HUMIDITY_LIMIT", DEFAULT_HUMIDITY_LIMIT));

    public Limit getLimit(String sensorId, String type) {
        if ("temperature".equalsIgnoreCase(type)) {
            return temperatureLimit;
        }
        if ("humidity".equalsIgnoreCase(type)) {
            return humidityLimit;
        }
        return null;
    }

    private static double env(String name, double fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : Double.parseDouble(value.trim());
    }
}
