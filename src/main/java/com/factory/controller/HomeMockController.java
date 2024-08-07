package com.factory.controller;

import com.factory.openapi.api.HomeApi;
import com.factory.openapi.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@RestController
@CrossOrigin
public class HomeMockController implements HomeApi {
    private List<SensorValue> currentSensorValues;
    private List<SensorValue> averageSensorValues;
    private Map<UUID, ChartConfig> chartConfigs;

    public HomeMockController() {
        // Initialize with default values
        this.currentSensorValues = new ArrayList<>(List.of(
                new SensorValue("Pressure after compressor", "5.4 MPa"),
                new SensorValue("Temperature before compressor", "300 K"),
                new SensorValue("Temperature in combustion chamber", "700 K"),
                new SensorValue("Input flow rate", "4 m³/min"),
                new SensorValue("Output flow rate", "2.3 m³/min"),
                new SensorValue("Input gas composition", "42 % CO₂, 18 % H₂, 10 % NH₃, 15 % O₂, 15% N₂")
        ));
        this.averageSensorValues = new ArrayList<>(this.currentSensorValues);
        this.chartConfigs = new HashMap<>();
        addDefaultChartConfigs();
    }

    private void addDefaultChartConfigs() {
        addChartConfig(new ChartConfigInput("Temperature before compressor", "temperature", BigDecimal.valueOf(5), BigDecimal.valueOf(40), "K"));
        addChartConfig(new ChartConfigInput("Pressure after compressor", "pressure", BigDecimal.valueOf(2), BigDecimal.valueOf(8), "MPa"));
        addChartConfig(new ChartConfigInput("Flow rate", "flow", BigDecimal.valueOf(0), BigDecimal.valueOf(300), "m³/h"));
    }

    private ChartConfig addChartConfig(ChartConfigInput input) {
        UUID id = UUID.randomUUID();
        ChartConfig config = new ChartConfig()
                .id(id)
                .title(input.getTitle())
                .sensorType(input.getSensorType())
                .minDomain(input.getMinDomain())
                .maxDomain(input.getMaxDomain())
                .unit(input.getUnit());
        chartConfigs.put(id, config);
        return config;
    }

    private List<ChartDataPoint> generateData(String sensorType, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(days);
        List<ChartDataPoint> data = new ArrayList<>();
        Random random = new Random();

        double min, max;
        max = switch (sensorType) {
            case "temperature" -> {
                min = 5;
                yield 40;
            }
            case "pressure" -> {
                min = 2;
                yield 8;
            }
            case "flow" -> {
                min = 0;
                yield 300;
            }
            default -> {
                min = 0;
                yield 100;
            }
        };

        while (startDate.isBefore(now) || startDate.isEqual(now)) {
            data.add(new ChartDataPoint()
                    .time(BigDecimal.valueOf(startDate.toInstant(ZoneOffset.UTC).toEpochMilli()))
                    .value(BigDecimal.valueOf(random.nextDouble() * (max - min) + min))
            );
            startDate = startDate.plusMinutes(15);
        }

        return data;
    }

    private int getDaysFromTimeRange(String timeRange) {
        return switch (timeRange) {
            case "twoLastDays" -> 2;
            case "threeLastDays" -> 3;
            case "fiveLastDays" -> 5;
            case "lastWeek" -> 7;
            default -> 1;
        };
    }

    @Override
    public ResponseEntity<ChartConfig> addHomeChartConfig(@Valid ChartConfigInput chartConfigInput) {
        ChartConfig newConfig = addChartConfig(chartConfigInput);
        return ResponseEntity.ok(newConfig);
    }

    @Override
    public ResponseEntity<SensorValue> addHomeSensorValue(@Valid SensorValue sensorValue) {
        currentSensorValues.add(sensorValue);
        return ResponseEntity.ok(sensorValue);
    }

    @Override
    public ResponseEntity<List<ChartConfig>> getHomeChartConfigs() {
        return ResponseEntity.ok(new ArrayList<>(chartConfigs.values()));
    }

    @Override
    public ResponseEntity<ChartConfig> getHomeChartConfig(UUID id) {
        ChartConfig config = chartConfigs.get(id);
        if (config != null) {
            return ResponseEntity.ok(config);
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<ChartConfig> updateHomeChartConfig(UUID id, @Valid ChartConfigInput chartConfigInput) {
        if (chartConfigs.containsKey(id)) {
            ChartConfig updatedConfig = new ChartConfig()
                    .id(id)
                    .title(chartConfigInput.getTitle())
                    .sensorType(chartConfigInput.getSensorType())
                    .minDomain(chartConfigInput.getMinDomain())
                    .maxDomain(chartConfigInput.getMaxDomain())
                    .unit(chartConfigInput.getUnit());
            chartConfigs.put(id, updatedConfig);
            return ResponseEntity.ok(updatedConfig);
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<Void> deleteHomeChartConfig(UUID id) {
        if (chartConfigs.remove(id) != null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }


    @Override
    public ResponseEntity<Void> deleteHomeSensorValue(Integer index) {
        if (index >= 0 && index < currentSensorValues.size()) {
            currentSensorValues.remove((int)index);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<List<SensorValue>> getHomeAverageSensorValues() {
        return ResponseEntity.ok(List.of(
                new SensorValue("Pressure after compressor", "5.4 MPa"),
                new SensorValue("Temperature before compressor", "300 K"),
                new SensorValue("Temperature in combustion chamber", "700 K"),
                new SensorValue("Input flow rate", "4 m³/min"),
                new SensorValue("Output flow rate", "2.3 m³/min"),
                new SensorValue("Input gas composition", "42 % CO₂, 18 % H₂, 10 % NH₃, 15 % O₂, 15% N₂")
        ));
    }

    @Override
    public ResponseEntity<List<ChartDataPoint>> getHomeChartData(@NotNull @Valid String sensorType, @NotNull @Valid String timeRange) {
        int days = getDaysFromTimeRange(timeRange);
        List<ChartDataPoint> data = generateData(sensorType, days);
        return ResponseEntity.ok(data);
    }

    @Override
    public ResponseEntity<List<Event>> getHomeEvents(@Valid Boolean showOnlyAlerts, @Valid String searchTerm, @Valid LocalDate startDate, @Valid LocalDate endDate) {
        List<Event> events = new ArrayList<>();

        // Add events to the list
        events.add(new Event("Speed workout at the track", LocalDateTime.of(2024, 6, 27, 18, 0).atOffset(ZoneOffset.MIN), true));
        events.add(new Event("Speed workout at the track", LocalDateTime.of(2024, 6, 27, 18, 0).atOffset(ZoneOffset.MIN), true));
        events.add(new Event("Long run from the club house", LocalDateTime.of(2024, 6, 30, 8, 0).atOffset(ZoneOffset.MIN), false));
        events.add(new Event("Speed workout at the track", LocalDateTime.of(2024, 6, 27, 18, 0).atOffset(ZoneOffset.MIN), true));
        events.add(new Event("Long run from the club house", LocalDateTime.of(2024, 6, 30, 8, 0).atOffset(ZoneOffset.MIN), false));
        events.add(new Event("Speed workout at the track", LocalDateTime.of(2024, 6, 27, 18, 0).atOffset(ZoneOffset.MIN), true));
        events.add(new Event("Hill repeats on 5th Street", LocalDateTime.of(2024, 7, 2, 19, 0).atOffset(ZoneOffset.MIN), false));
        events.add(new Event("Hill repeats on 5th Street", LocalDateTime.of(2024, 7, 2, 19, 0).atOffset(ZoneOffset.MIN), false));
        events.add(new Event("Hill repeats on 5th Street", LocalDateTime.of(2024, 7, 2, 19, 0).atOffset(ZoneOffset.MIN), false));

        return ResponseEntity.ok(events);
    }

    @Override
    public ResponseEntity<List<SensorValue>> getHomeSensorValues() {
        List<SensorValue> sensorValues = List.of(
                new SensorValue("Pressure after compressor", "5.4 MPa"),
                new SensorValue("Temperature before compressor", "300 K"),
                new SensorValue("Temperature in combustion chamber", "700 K"),
                new SensorValue("Input flow rate", "4 m³/min"),
                new SensorValue("Output flow rate", "2.3 m³/min"),
                new SensorValue("Input gas composition", "42 % CO₂, 18 % H₂, 10 % NH₃, 15 % O₂, 15% N₂")
        );
        return ResponseEntity.ok(sensorValues);
    }

    @Override
    public ResponseEntity<SensorValue> updateHomeSensorValue(Integer index, @Valid SensorValue sensorValue) {
        if (index >= 0 && index < currentSensorValues.size()) {
            currentSensorValues.set(index, sensorValue);
            return ResponseEntity.ok(sensorValue);
        }
        return ResponseEntity.notFound().build();
    }
}
