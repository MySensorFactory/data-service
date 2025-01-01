package com.factory.mapping;

import com.factory.domain.SensorDataEntry;
import com.factory.openapi.model.DashboardConfig;
import com.factory.openapi.model.SensorValue;
import com.factory.openapi.model.ValueConfig;
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

    @Mapping(target = "currentSensorValuesConfig", expression = """
            java(valueConfigs.stream().filter(ValueConfig::isCurrent).toList())
            """)
    List<ValueConfig> mapCurrentSensorValues(List<com.factory.persistence.home.entity.ValueConfig> valueConfigs);

    @Mapping(target = "currentSensorValuesConfig", expression = """
            java(valueConfigs.stream().filter(v -> !v.isCurrent()).toList())
            """)
    List<ValueConfig> mapAverageSensorValues(List<com.factory.persistence.home.entity.ValueConfig> valueConfigs);

    default List<com.factory.persistence.home.entity.ValueConfig> mapAverageSensorValuesToEntities(final com.factory.persistence.home.entity.DashboardConfig config,
                                                                                                   final List<ValueConfig> valueConfigs){
        return  valueConfigs.stream().map(v -> mapAverageSensorValue(config,v)).toList();
    }

    default List<com.factory.persistence.home.entity.ValueConfig> mapCurrentSensorValuesToEntities(final com.factory.persistence.home.entity.DashboardConfig config,
                                                                                                   final List<ValueConfig> valueConfigs){
        return  valueConfigs.stream().map(v -> mapCurrentSensorValue(config,v)).toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "current", expression = "java(true)")
    @Mapping(target = "dashboardConfig", source = "dashboardConfig")
    @Mapping(target = "label", source = "valueConfig.label")
    @Mapping(target = "sensorType", source = "valueConfig.sensorType")
    com.factory.persistence.home.entity.ValueConfig mapCurrentSensorValue(com.factory.persistence.home.entity.DashboardConfig dashboardConfig, ValueConfig valueConfig);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "current", expression = "java(false)")
    @Mapping(target = "dashboardConfig", source = "dashboardConfig")
    @Mapping(target = "label", source = "valueConfig.label")
    @Mapping(target = "sensorType", source = "valueConfig.sensorType")
    com.factory.persistence.home.entity.ValueConfig mapAverageSensorValue(com.factory.persistence.home.entity.DashboardConfig dashboardConfig, ValueConfig valueConfig);

    List<ChartConfig> map(com.factory.persistence.home.entity.DashboardConfig config, List<com.factory.openapi.model.ChartConfig> chartConfigs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dashboardConfig", source = "config")
    @Mapping(target = "sensorType", source = "chartConfig.sensorType")
    @Mapping(target = "label", source = "chartConfig.label")
    ChartConfig map(com.factory.persistence.home.entity.DashboardConfig config, com.factory.openapi.model.ChartConfig chartConfig);

    @Mapping(target = "values", source = "data")
    SensorValue map(SensorDataEntry entry);

    List<SensorValue> mapSensorValues(Iterable<SensorDataEntry> entries);
}
