package com.factory.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SensorDataEntry extends BasicSensorDataEntry {

    private UUID id;
    private SensorType sensorType;
    private SensorLabel label;
}
