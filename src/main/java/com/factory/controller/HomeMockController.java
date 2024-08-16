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
    private final Map<String, DashboardConfig> dashboardConfigs = new HashMap<>();

    public HomeMockController() {
        // Initialize with default values
        var config = new DashboardConfig()
                .id(UUID.fromString("038833bf-9efb-40a2-945f-4b7ea29354d4"))
                .currentSensorValuesConfig(new ArrayList<>(List.of(
                        new ValueConfig(UUID.randomUUID(), "Pressure after compressor", "pressure"),
                        new ValueConfig(UUID.randomUUID(),"Temperature before compressor", "temperature"),
                        new ValueConfig(UUID.randomUUID(),"Temperature in combustion chamber", "temperature"),
                        new ValueConfig(UUID.randomUUID(),"Input flow rate", "flow"),
                        new ValueConfig(UUID.randomUUID(),"Output flow rate", "flow"),
                        new ValueConfig(UUID.randomUUID(),"Input gas composition", "composition")
                )))
                .averageSensorValuesConfig(new ArrayList<>(List.of(
                        new ValueConfig(UUID.randomUUID(), "Pressure after compressor", "pressure"),
                        new ValueConfig(UUID.randomUUID(),"Temperature before compressor", "temperature"),
                        new ValueConfig(UUID.randomUUID(),"Temperature in combustion chamber", "temperature"),
                        new ValueConfig(UUID.randomUUID(),"Input flow rate", "flow"),
                        new ValueConfig(UUID.randomUUID(),"Output flow rate", "flow"),
                        new ValueConfig(UUID.randomUUID(),"Input gas composition", "composition")
                )))
                .chartConfigs(new ArrayList<>());
        dashboardConfigs.put("038833bf-9efb-40a2-945f-4b7ea29354d4", config);
        addDefaultChartConfigs();
    }

    private void addChartConfig(String id, ChartConfig input) {
        ChartConfig config = new ChartConfig()
                .label(input.getLabel())
                .sensorType(input.getSensorType());
        this.dashboardConfigs.get(id).getChartConfigs().add(config);
    }

    private void addDefaultChartConfigs() {
        addChartConfig("038833bf-9efb-40a2-945f-4b7ea29354d4", new ChartConfig(UUID.randomUUID(),"Temperature before compressor", "temperature"));
        addChartConfig("038833bf-9efb-40a2-945f-4b7ea29354d4", new ChartConfig(UUID.randomUUID(),"Pressure after compressor", "pressure"));
        addChartConfig("038833bf-9efb-40a2-945f-4b7ea29354d4", new ChartConfig(UUID.randomUUID(),"Flow rate", "flowRate"));
    }

    @Override
    public ResponseEntity<DashboardConfig> getDashboardConfig(UUID id) {
        return ResponseEntity.ok(this.dashboardConfigs.get(id.toString()));
    }

    @Override
    public ResponseEntity<DashboardConfig> updateDashboardConfig(UUID id, @Valid DashboardConfig dashboardConfig) {

        dashboardConfig.getChartConfigs().forEach(c -> {
            if (c.getId() == null){
                c.setId(UUID.randomUUID());
            }
        });

        dashboardConfig.getCurrentSensorValuesConfig().forEach(c -> {
            if (c.getId() == null){
                c.setId(UUID.randomUUID());
            }
        });

        dashboardConfig.getAverageSensorValuesConfig().forEach(c -> {
            if (c.getId() == null) {
                c.setId(UUID.randomUUID());
            }
        });

        this.dashboardConfigs.put(id.toString(), dashboardConfig);
        return ResponseEntity.ok(this.dashboardConfigs.get(id.toString()));
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
    public ResponseEntity<List<SensorValue>> getHomeAverageSensorValues(UUID dashboardId) {
        return ResponseEntity.ok(List.of(
                new SensorValue("5.4 MPa", UUID.randomUUID(),"Pressure after compressor", "pressure"),
                new SensorValue("300 K", UUID.randomUUID(),"Temperature before compressor", "temperature"),
                new SensorValue("700 K", UUID.randomUUID(),"Temperature in combustion chamber", "temperature"),
                new SensorValue("4 m³/min", UUID.randomUUID(),"Input flow rate", "flow"),
                new SensorValue("2.3 m³/min", UUID.randomUUID(),"Output flow rate", "flow"),
                new SensorValue("42 % CO₂, 18 % H₂, 10 % NH₃, 15 % O₂, 15% N₂", UUID.randomUUID(),"Input gas composition", "composition")
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
    public ResponseEntity<List<SensorValue>> getHomeSensorValues(UUID dashboardId) {

        List<SensorValue> sensorValues = dashboardConfigs.get(dashboardId.toString())
                .getCurrentSensorValuesConfig().stream()
                .map(c -> SensorValue.builder()
                        .id(c.getId())
                        .value(generateSnesorData(c.getSensorType()))
                        .sensorType(c.getSensorType())
                        .label(c.getLabel())
                        .build())
                .toList();
        return ResponseEntity.ok(sensorValues);
    }

    private String generateSnesorData(String sensorType) {
        return switch (sensorType) {
            case "temperature" -> "500 K";
            case "pressure" -> "5 MPa";
            case "flow" -> "100 m3/min";
            case "composition" -> "42 % CO₂, 18 % H₂, 10 % NH₃, 15 % O₂, 15% N₂";
            default -> null;
        };

    }

}
