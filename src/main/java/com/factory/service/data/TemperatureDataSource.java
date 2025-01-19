package com.factory.service.data;

import com.factory.config.dto.DataSourceConfig;
import com.factory.domain.BasicSensorDataEntry;
import com.factory.domain.SensorLabel;
import com.factory.mapping.SensorDataMapper;
import com.factory.persistence.data.repository.MeanTemperatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemperatureDataSource implements SensorDataSource {

    private final DataSourceConfig dataSourceConfig;
    private final MeanTemperatureRepository meanTemperatureRepository;
    private final SensorDataMapper sensorDataMapper;

    @Override
    public List<BasicSensorDataEntry> findByLabelAndTimeWindow(final SensorLabel label,
                                                               final ZonedDateTime from,
                                                               final ZonedDateTime to) {
        return meanTemperatureRepository.findByTimeWindowAndLabel(label.getLabel(), from, to)
                .stream()
                .map(sensorDataMapper::map)
                .toList();
    }

    @Override
    public String getSensorType() {
        return dataSourceConfig.getDataSources().get("temperature").getSensorType();
    }
}
