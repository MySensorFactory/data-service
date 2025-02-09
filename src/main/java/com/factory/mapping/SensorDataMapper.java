package com.factory.mapping;

import com.factory.domain.SensorDataEntry;
import com.factory.persistence.data.entity.SensorData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;
import java.util.stream.Collectors;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        uses = {CommonMapper.class},
        imports = {Map.class}
)
public interface SensorDataMapper {

    @Mapping(target = "label", source = "auditData.label")
    @Mapping(target = "timestamp", source = "auditData.timestamp")
    @Mapping(target = "data", source = "data")
    SensorDataEntry map(SensorData sensorData);

    default Map<String, Double> map(Map<String, Object> value) {
        return value.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (Double) entry.getValue()
                )
        );
    }

}