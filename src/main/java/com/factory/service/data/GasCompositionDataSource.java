package com.factory.service.data;

import com.factory.config.dto.DataSourceConfig;
import com.factory.mapping.SensorDataMapper;
import com.factory.persistence.data.repository.SensorDataRepository;
import org.springframework.stereotype.Service;

@Service
public class GasCompositionDataSource extends SensorDataSource {
    private final DataSourceConfig dataSourceConfig;

    public GasCompositionDataSource(final SensorDataRepository sensorDataRepository,
                                    final SensorDataMapper sensorDataMapper,
                                    final DataSourceConfig dataSourceConfig) {
        super(sensorDataRepository, sensorDataMapper);
        this.dataSourceConfig = dataSourceConfig;
    }

    @Override
    public String getSensorType() {
        return dataSourceConfig.getDataSources().get("gasComposition").getSensorType();
    }
}
