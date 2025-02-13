package com.factory.mapping;

import com.factory.config.dto.DataConfig;
import com.factory.config.dto.DataSourceConfig;
import com.factory.openapi.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface DataConfigMapper {
    @Mapping(target = "dataSources", source = "dataSourceConfig.dataSources")
    @Mapping(target = "sortOptions", source = "dataConfig.sortOptions")
    @Mapping(target = "timeRangeOptions", source = "dataConfig.timeRangeOptions")
    @Mapping(target = "unitMapping", source = "dataConfig.unitMapping")
    @Mapping(target = "wideSensors", source = "dataConfig.wideSensors")
    Config dataConfigToConfig(DataConfig dataConfig, DataSourceConfig dataSourceConfig);

    LabeledValue map(DataConfig.LabeledValue labeledValue);

    TimeRangeOption map(DataConfig.TimeRangeOption timeRangeOption);

    DataSource map(DataSourceConfig.DataSource dataSource);

    SensorLabel map(DataSourceConfig.Label label);

    Map<String, DataSource> mapDataSources(Map<String, DataSourceConfig.DataSource> dataSources);
}