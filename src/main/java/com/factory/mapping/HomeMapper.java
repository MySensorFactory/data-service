package com.factory.mapping;

import com.factory.domain.BasicSensorDataEntry;
import com.factory.domain.SensorDataEntry;
import com.factory.openapi.model.*;
import com.factory.persistence.home.entity.ChartConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        uses = {CommonMapper.class},
        imports = {Map.class}
)
public interface HomeMapper {

    @Mapping(target = "currentSensorValuesConfig", expression = """
            java(mapCurrentSensorValues(dashboardConfig.getValueConfigs()))
            """)
    @Mapping(target = "averageSensorValuesConfig", expression = """
            java(mapAverageSensorValues(dashboardConfig.getValueConfigs()))
            """)
    DashboardConfig map(com.factory.persistence.home.entity.DashboardConfig dashboardConfig);

    default List<ValueConfig> mapCurrentSensorValues(List<com.factory.persistence.home.entity.ValueConfig> valueConfigs){
        return valueConfigs.stream()
                .filter(com.factory.persistence.home.entity.ValueConfig::isCurrent)
                .map(this::map)
                .toList();
    }

    default List<ValueConfig> mapAverageSensorValues(List<com.factory.persistence.home.entity.ValueConfig> valueConfigs){
        return valueConfigs.stream()
                .filter(e -> !e.isCurrent())
                .map(this::map)
                .toList();
    }

    ValueConfig map(com.factory.persistence.home.entity.ValueConfig valueConfig);

    default List<com.factory.persistence.home.entity.ValueConfig> mapAverageSensorValuesToEntities(final com.factory.persistence.home.entity.DashboardConfig config,
                                                                                                   final List<ValueConfig> valueConfigs) {
        return valueConfigs.stream().map(v -> mapAverageSensorValue(config, v)).toList();
    }

    default List<com.factory.persistence.home.entity.ValueConfig> mapCurrentSensorValuesToEntities(final com.factory.persistence.home.entity.DashboardConfig config,
                                                                                                   final List<ValueConfig> valueConfigs) {
        return valueConfigs.stream().map(v -> mapCurrentSensorValue(config, v)).toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "current", expression = "java(true)")
    @Mapping(target = "label", source = "valueConfig.label")
    @Mapping(target = "sensorType", source = "valueConfig.sensorType")
    com.factory.persistence.home.entity.ValueConfig mapCurrentSensorValue(com.factory.persistence.home.entity.DashboardConfig dashboardConfig, ValueConfig valueConfig);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "current", expression = "java(false)")
    @Mapping(target = "label", source = "valueConfig.label")
    @Mapping(target = "sensorType", source = "valueConfig.sensorType")
    com.factory.persistence.home.entity.ValueConfig mapAverageSensorValue(com.factory.persistence.home.entity.DashboardConfig dashboardConfig, ValueConfig valueConfig);

    default List<ChartConfig> map(com.factory.persistence.home.entity.DashboardConfig config,
                          List<com.factory.openapi.model.ChartConfig> chartConfigs){
        return chartConfigs.stream().map(c -> map(config, c)).toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dashboardConfig", source = "config")
    @Mapping(target = "sensorType", source = "chartConfig.sensorType")
    @Mapping(target = "label", source = "chartConfig.label")
    ChartConfig map(com.factory.persistence.home.entity.DashboardConfig config, com.factory.openapi.model.ChartConfig chartConfig);

    @Mapping(target = "values", source = "data")
    SensorValue map(SensorDataEntry entry);

    List<SensorValue> mapSensorValues(Iterable<SensorDataEntry> entries);

    @Mapping(target = "values", source = "data")
    SensorData map(BasicSensorDataEntry entry);

    List<SensorData> mapBasicSensorValues(Iterable<BasicSensorDataEntry> entries);

    Event map(com.factory.domain.Event event);

    com.factory.domain.Event map(com.factory.persistence.home.entity.Event event);
}
