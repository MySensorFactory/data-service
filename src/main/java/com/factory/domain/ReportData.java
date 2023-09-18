package com.factory.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ReportData {
    private UUID id;
    private Long from;
    private Long to;
    private Map<SensorType, Map<EventKey,SensorDataEntry>> sensorsInstantEntries;
}
