package com.factory.service;

import com.factory.domain.BasicSensorDataEntry;
import com.factory.domain.SensorDataEntry;
import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;
import com.factory.mapping.SensorDataMapper;
import com.factory.persistence.home.entity.ValueConfig;
import com.factory.service.data.SensorDataSourceResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultSensorsService implements SensorsService {

    private final SensorDataSourceResolver sensorDataSourceResolver;
    private final CurrentSensorValuesService currentSensorValuesService;
    private final SensorDataMapper sensorDataMapper;

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
    public Set<SensorDataEntry> getLatestCurrentSensorData(final List<ValueConfig> valueConfigs) {
        return valueConfigs.stream()
                .map(c -> {
                            var result = switch (c.getSensorType()) {
                                case "temperature" ->
                                        sensorDataMapper.map(currentSensorValuesService.getCurrentTemperature(SensorLabel.of(c.getLabel())));
                                case "pressure" ->
                                        sensorDataMapper.map(currentSensorValuesService.getCurrentPressure(SensorLabel.of(c.getLabel())));
                                case "flowRate" ->
                                        sensorDataMapper.map(currentSensorValuesService.getCurrentFlowRate(SensorLabel.of(c.getLabel())));
                                case "gasComposition" ->
                                        sensorDataMapper.map(currentSensorValuesService.getCurrentGasComposition(SensorLabel.of(c.getLabel())));
                                case "compressorState" ->
                                        sensorDataMapper.map(currentSensorValuesService.getCurrentNoiseAndVibration(SensorLabel.of(c.getLabel())));
                                default ->
                                        throw new IllegalArgumentException("Unsupported sensor type: " + c.getSensorType());
                            };
                            result.setId(c.getId());
                            return result;
                        }
                )
                .collect(Collectors.toSet());
    }

    @Override
    public Set<SensorDataEntry> getLatestAverageSensorData(final List<ValueConfig> valueConfigs) {
        return valueConfigs.stream().map(
                        entry -> {
                            var result = sensorDataSourceResolver.getDataSource(SensorType.of(entry.getSensorType()))
                                    .findLatest(SensorLabel.of(entry.getLabel()));
                            result.ifPresent(sensorDataEntry -> sensorDataEntry.setId(entry.getId()));
                            return result;
                        }
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

}
