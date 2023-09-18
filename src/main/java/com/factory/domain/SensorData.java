package com.factory.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SensorData {
    private Long from;
    private Long to;
    private String label;
    private String sensorType;
    private Map<EventKey, SensorDataEntry> entries;
}
