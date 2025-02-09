package com.factory.service.data;

import com.factory.domain.BasicSensorDataEntry;
import com.factory.domain.SensorDataEntry;
import com.factory.domain.SensorLabel;
import com.factory.mapping.SensorDataMapper;
import com.factory.persistence.data.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

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

    public SensorDataEntry findLatest(final SensorLabel label) {
        return sensorDataMapper.map(sensorDataRepository.findLatest(getSensorType(), label.getLabel()));
    }

    abstract String getSensorType();
}
