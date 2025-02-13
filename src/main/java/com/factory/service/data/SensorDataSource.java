package com.factory.service.data;

import com.factory.domain.BasicSensorDataEntry;
import com.factory.domain.SensorDataEntry;
import com.factory.domain.SensorLabel;
import com.factory.mapping.SensorDataMapper;
import com.factory.persistence.data.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class SensorDataSource {
    private final SensorDataRepository sensorDataRepository;
    private final SensorDataMapper sensorDataMapper;

    public List<BasicSensorDataEntry> findByLabelAndTimeWindow(final SensorLabel label,
                                                               final ZonedDateTime from,
                                                               final ZonedDateTime to) {
        return sensorDataRepository
                .findByTimeWindowAndLabelAndType(label.getLabel(), from, to, getSensorType())
                .stream()
                .map(entry -> (BasicSensorDataEntry) sensorDataMapper.map(entry))
                .toList();
    }

    public Optional<SensorDataEntry> findLatest(final SensorLabel label) {
        var result = sensorDataRepository.findLatest(getSensorType(), label.getLabel());
        return result.map(sensorDataMapper::map);
    }

    abstract String getSensorType();
}
