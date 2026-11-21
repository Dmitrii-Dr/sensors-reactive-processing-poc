package com.dmdr.monitoring.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Measurement(String deviceId, String value, String type, Instant dateTime) {
}
