package com.factory.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ReportData {
    private UUID id;
    private String name;
    private Long from;
    private Long to;
    private String label;
    private String description;
    private Map<SensorType, List<BasicSensorDataEntry>> sensorsDataEntries;
}
