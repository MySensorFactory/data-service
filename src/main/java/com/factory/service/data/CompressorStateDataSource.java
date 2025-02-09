package com.factory.service.data;

import com.factory.config.dto.DataSourceConfig;
import com.factory.mapping.SensorDataMapper;
import com.factory.persistence.data.repository.SensorDataRepository;
import org.springframework.stereotype.Service;

@Service
public class CompressorStateDataSource extends SensorDataSource {
    private final DataSourceConfig dataSourceConfig;

    public CompressorStateDataSource(final SensorDataRepository sensorDataRepository,
                                     final SensorDataMapper sensorDataMapper,
                                     final DataSourceConfig dataSourceConfig) {
        super(sensorDataRepository, sensorDataMapper);
        this.dataSourceConfig = dataSourceConfig;
    }

    @Override
    public String getSensorType() {
        return dataSourceConfig.getDataSources().get("compressorState").getSensorType();
    }
}
