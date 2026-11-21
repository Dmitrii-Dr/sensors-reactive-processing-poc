package com.dmdr.monitoring.alert;

public class Limit implements Comparable<Double> {
    private final double limit;

    public Limit(double limit) {
        this.limit = limit;
    }

    public double value() {
        return limit;
    }

    @Override
    public int compareTo(Double other) {
        return Double.compare(limit, other);
    }

    @Override
    public String toString() {
        return String.valueOf(limit);
    }
}
