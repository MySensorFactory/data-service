package com.factory.mapping;

import com.factory.domain.BasicSensorDataEntry;
import com.factory.persistence.data.entity.MeanPressure;
import com.factory.persistence.data.entity.MeanFlowRate;
import com.factory.persistence.data.entity.CompressorState;
import com.factory.persistence.data.entity.MeanGasComposition;
import com.factory.persistence.data.entity.MeanTemperature;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        uses = {CommonMapper.class},
        imports = {Map.class}
)
public interface SensorDataMapper {

    @Mapping(target = "timestamp", source = "auditData.timestamp")
    @Mapping(target = "data", expression = "java(Map.of(\"pressureMean\", meanPressure.getValue()))")
    BasicSensorDataEntry map(MeanPressure meanPressure);

    @Mapping(target = "timestamp", source = "auditData.timestamp")
    @Mapping(target = "data", expression = "java(Map.of(\"flowRateMean\", meanFlowRate.getValue()))")
    BasicSensorDataEntry map(MeanFlowRate meanFlowRate);

    @Mapping(target = "timestamp", source = "auditData.timestamp")
    @Mapping(target = "data", expression = "java(Map.of(\"noiseLevel\", compressorState.getNoiseLevel(), \"vibrationAmplitude\", compressorState.getVibrationAmplitude(), \"vibrationFrequency\", compressorState.getVibrationFrequency()))")
    BasicSensorDataEntry map(CompressorState compressorState);

    @Mapping(target = "timestamp", source = "auditData.timestamp")
    @Mapping(target = "data", expression = "java(Map.of(\"co2\", meanGasComposition.getCo2(), \"h2\", meanGasComposition.getH2(), \"nh3\", meanGasComposition.getNh3(), \"n2\", meanGasComposition.getN2(), \"o2\", meanGasComposition.getO2()))")
    BasicSensorDataEntry map(MeanGasComposition meanGasComposition);

    @Mapping(target = "timestamp", source = "auditData.timestamp")
    @Mapping(target = "data", expression = "java(Map.of(\"temperatureMean\", meanTemperature.getValue()))")
    BasicSensorDataEntry map(MeanTemperature meanTemperature);
}