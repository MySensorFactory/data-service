package com.factory.service;

import com.factory.domain.BasicSensorDataEntry;
import com.factory.domain.SensorDataEntry;
import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;
import com.factory.persistence.home.entity.ValueConfig;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SensorsService {

    Map<SensorType, List<BasicSensorDataEntry>> getSensorsData(ZonedDateTime from,
                                                               ZonedDateTime to,
                                                               SensorLabel sensorLabel,
                                                               Set<SensorType> includedSensors);

    Set<SensorDataEntry> getLatestCurrentSensorData(List<ValueConfig> sensors);

    Set<SensorDataEntry> getLatestAverageSensorData(List<ValueConfig> sensors);
}
