package com.factory.service;

import com.factory.domain.SensorLabel;
import com.factory.message.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class CurrentSensorValuesService {

    private final Map<SensorLabel, Pressure> currentPressure = new HashMap<>();
    private final Map<SensorLabel, Temperature> currentTemperature = new HashMap<>();
    private final Map<SensorLabel, FlowRate> currentFlowRate = new HashMap<>();
    private final Map<SensorLabel, GasComposition> currentGasComposition = new HashMap<>();
    private final Map<SensorLabel, NoiseAndVibration> currentNoiseAndVibration = new HashMap<>();

    public Pressure getCurrentPressure(final SensorLabel label) {
        return currentPressure.get(label);
    }

    public Temperature getCurrentTemperature(final SensorLabel label) {
        return currentTemperature.get(label);
    }

    public FlowRate getCurrentFlowRate(final SensorLabel label) {
        return currentFlowRate.get(label);
    }

    public GasComposition getCurrentGasComposition(final SensorLabel label) {
        return currentGasComposition.get(label);
    }

    public NoiseAndVibration getCurrentNoiseAndVibration(final SensorLabel label) {
        return currentNoiseAndVibration.get(label);
    }

    @ConditionalOnProperty(
            value = "data.dataSources.temperature.sensorType",
            havingValue = "temperature"
    )
    @KafkaListener(
            topics = "temperature",
            autoStartup = "true",
            concurrency = "1",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(@Payload final Temperature message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        currentTemperature.put(SensorLabel.of(message.getLabel().toString()), message);
    }

    @ConditionalOnProperty(
            value = "data.dataSources.pressure.sensorType",
            havingValue = "pressure"
    )
    @KafkaListener(
            topics = "pressure",
            autoStartup = "true",
            concurrency = "1",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(@Payload final Pressure message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        currentPressure.put(SensorLabel.of(message.getLabel().toString()), message);
    }

    @ConditionalOnProperty(
            value = "data.dataSources.flowRate.sensorType",
            havingValue = "flowRate"
    )
    @KafkaListener(
            topics = "flowRate",
            autoStartup = "true",
            concurrency = "1",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(@Payload final FlowRate message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        currentFlowRate.put(SensorLabel.of(message.getLabel().toString()), message);
    }

    @ConditionalOnProperty(
            value = "data.dataSources.gasComposition.sensorType",
            havingValue = "gasComposition"
    )
    @KafkaListener(
            topics = "gasComposition",
            autoStartup = "true",
            concurrency = "1",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(@Payload final GasComposition message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        currentGasComposition.put(SensorLabel.of(message.getLabel().toString()), message);
    }

    @ConditionalOnProperty(
            value = "data.dataSources.compressorState.sensorType",
            havingValue = "compressorState"
    )
    @KafkaListener(
            topics = "noiseAndVibration",
            autoStartup = "true",
            concurrency = "1",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listen(@Payload final NoiseAndVibration message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        currentNoiseAndVibration.put(SensorLabel.of(message.getLabel().toString()), message);
    }

}