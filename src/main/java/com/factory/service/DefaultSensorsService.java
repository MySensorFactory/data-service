package com.factory.service;

import com.factory.domain.BasicSensorDataEntry;
import com.factory.domain.SensorDataEntry;
import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;
import com.factory.service.data.SensorDataSourceResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultSensorsService implements SensorsService {

    private final SensorDataSourceResolver sensorDataSourceResolver;

    @Override
    public Map<SensorType, List<BasicSensorDataEntry>> getSensorsData(final ZonedDateTime from,
                                                                      final ZonedDateTime to,
                                                                      final SensorLabel sensorLabel,
                                                                      final Set<SensorType> sensorTypes) {

        if (CollectionUtils.isEmpty(sensorTypes)) {
            return Map.of();
        }

        return sensorTypes
                .stream()
                .collect(
                        Collectors.toMap(Function.identity(),
                                entry ->
                                        sensorDataSourceResolver.getDataSource(entry)
                                                .findByLabelAndTimeWindow(sensorLabel, from, to)
                        ));
    }

    @Override
    public Set<SensorDataEntry> getLatestCurrentSensorData(Set<Pair<SensorLabel, SensorType>> sensors) {
        return Set.of();
    }

    @Override
    public Set<SensorDataEntry> getLatestAverageSensorData(Set<Pair<SensorLabel, SensorType>> sensors) {
        return Set.of();
    }

}
