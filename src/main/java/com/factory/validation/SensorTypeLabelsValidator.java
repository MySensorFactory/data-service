package com.factory.validation;

import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;

import java.util.Map;

public interface SensorTypeLabelsValidator {

    void validate(Map<SensorType, SensorLabel> input);

    void validate(SensorType sensorType, SensorLabel label);
}
