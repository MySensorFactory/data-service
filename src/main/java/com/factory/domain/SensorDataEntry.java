package com.factory.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SensorDataEntry {
    private Long timestamp;
    private String label;
    private String eventKey;
    private String sensorType;
    private Map<String, Double> data;
}
