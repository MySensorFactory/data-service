package com.factory.config.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DataSource {
    private String sensorType;
    private List<String> availableLabels;
}
