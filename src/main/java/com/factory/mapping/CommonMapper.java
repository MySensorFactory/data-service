package com.factory.mapping;

import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;
import com.factory.openapi.model.TimeRange;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface CommonMapper {

    default ZonedDateTime map(final Long timestamp) {
        if (timestamp != null) {
            Instant instant = Instant.ofEpochMilli(timestamp);
            return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        }
        return null;
    }

    default Long map(final ZonedDateTime input) {
        if (input != null) {
            return input.toInstant().toEpochMilli();
        }
        return null;
    }

    TimeRange map(final ZonedDateTime from, final ZonedDateTime to);

    default String map(final SensorLabel value){
        return value.getLabel();
    }

    default String map(final SensorType value){
        return value.getType();
    }

    default SensorLabel mapSensorLabel(String value){
        return SensorLabel.of(value);
    }

    default SensorType mapSensorType(String value){
        return SensorType.of(value);
    }
}
