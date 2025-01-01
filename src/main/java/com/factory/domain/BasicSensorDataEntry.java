package com.factory.domain;

import lombok.Data;

import java.util.Map;

@Data
public class BasicSensorDataEntry {
    protected Long timestamp;
    protected Map<String, Double> data;
}
