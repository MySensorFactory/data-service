package com.factory.service;

import com.factory.domain.BasicSensorDataEntry;
import com.factory.domain.SensorDataEntry;
import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;
import org.apache.commons.lang3.tuple.Pair;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SensorsService {

    Map<SensorType, List<BasicSensorDataEntry>> getSensorsData(ZonedDateTime from,
                                                               ZonedDateTime to,
                                                               SensorLabel sensorLabel,
                                                               Set<SensorType> includedSensors);

    Set<SensorDataEntry> getLatestCurrentSensorData(Set<Pair<SensorLabel, SensorType>> sensors);

    Set<SensorDataEntry> getLatestAverageSensorData(Set<Pair<SensorLabel, SensorType>> sensors);
}
