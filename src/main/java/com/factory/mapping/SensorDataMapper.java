package com.factory.mapping;

import com.factory.domain.SensorDataEntry;
import com.factory.message.*;
import com.factory.persistence.data.entity.SensorData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        uses = {CommonMapper.class},
        imports = {Map.class}
)
public interface SensorDataMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sensorType", constant = "pressure")
    @Mapping(target = "label", source = "source.label")
    @Mapping(target = "timestamp", source = "source.timestamp", qualifiedByName = "longToZonedDateTime")
    @Mapping(target = "data", source = "source", qualifiedByName = "pressureToJsonb")
    SensorDataEntry map(Pressure source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sensorType", constant = "temperature")
    @Mapping(target = "label", source = "source.label")
    @Mapping(target = "timestamp", source = "source.timestamp", qualifiedByName = "longToZonedDateTime")
    @Mapping(target = "data", source = "source", qualifiedByName = "temperatureToJsonb")
    SensorDataEntry map(Temperature source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sensorType", constant = "flowRate")
    @Mapping(target = "label", source = "source.label")
    @Mapping(target = "timestamp", source = "source.timestamp", qualifiedByName = "longToZonedDateTime")
    @Mapping(target = "data", source = "source", qualifiedByName = "flowRateToJsonb")
    SensorDataEntry map(FlowRate source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sensorType", constant = "gasComposition")
    @Mapping(target = "label", source = "source.label")
    @Mapping(target = "timestamp", source = "source.timestamp", qualifiedByName = "longToZonedDateTime")
    @Mapping(target = "data", source = "source", qualifiedByName = "gasCompositionToJsonb")
    SensorDataEntry map(GasComposition source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sensorType", constant = "compressorState")
    @Mapping(target = "label", source = "source.label")
    @Mapping(target = "timestamp", source = "source.timestamp", qualifiedByName = "longToZonedDateTime")
    @Mapping(target = "data", source = "source", qualifiedByName = "noiseAndVibrationToJsonb")
    SensorDataEntry map(NoiseAndVibration source);


    @Mapping(target = "label", source = "auditData.label")
    @Mapping(target = "timestamp", source = "auditData.timestamp")
    @Mapping(target = "data", source = "data")
    SensorDataEntry map(SensorData sensorData);

    @Named("longToZonedDateTime")
    default ZonedDateTime longToZonedDateTime(Long timestamp) {
        if (timestamp != null) {
            return ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
        }
        return null;
    }

    @Named("pressureToJsonb")
    default Map<String, Object> pressureToJsonb(Pressure source) {
        Map<String, Object> data = new HashMap<>();
        data.put("pressure", source.getData().getPressure());
        return data;
    }

    @Named("temperatureToJsonb")
    default Map<String, Object> temperatureToJsonb(Temperature source) {
        Map<String, Object> data = new HashMap<>();
        data.put("temperature", source.getData().getTemperature());
        return data;
    }

    @Named("flowRateToJsonb")
    default Map<String, Object> flowRateToJsonb(FlowRate source) {
        Map<String, Object> data = new HashMap<>();
        data.put("flowRate", source.getData().getFlowRate());
        return data;
    }

    @Named("gasCompositionToJsonb")
    default Map<String, Object> gasCompositionToJsonb(GasComposition source) {
        Map<String, Object> data = new HashMap<>();
        var gasData = source.getData();
        data.put("H2", gasData.getH2());
        data.put("N2", gasData.getN2());
        data.put("NH3", gasData.getNh3());
        data.put("O2", gasData.getO2());
        data.put("CO2", gasData.getCo2());
        return data;
    }

    @Named("noiseAndVibrationToJsonb")
    default Map<String, Object> noiseAndVibrationToJsonb(NoiseAndVibration source) {
        Map<String, Object> data = new HashMap<>();
        data.put("noiseLevel", source.getNoiseData().getLevel());
        data.put("vibrationAmplitude", source.getVibrationData().getAmplitude());
        data.put("vibrationFrequency", source.getVibrationData().getFrequency());
        return data;
    }

    default String map(CharSequence charSequence) {
        return charSequence.toString();
    }

    default Map<String, Double> map(Map<String, Object> value) {
        return value.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            if (entry.getValue() instanceof Float aFloat) {
                                return aFloat.doubleValue();
                            }

                            return (Double) entry.getValue();
                        }
                )
        );
    }

}