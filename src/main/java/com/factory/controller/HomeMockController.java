package com.factory.controller;

import com.factory.openapi.api.HomeApi;
import com.factory.openapi.model.*;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class HomeMockController implements HomeApi {
    public static final String DASHBOARD_CONFIG_ID = "damian";
    private final Map<String, DashboardConfig> dashboardConfigs = new HashMap<>();

    public HomeMockController() {
        // Initialize with default values
        var config = new DashboardConfig()
                .userName(DASHBOARD_CONFIG_ID)
                .currentSensorValuesConfig(new ArrayList<>(List.of(
                        new ValueConfig(UUID.randomUUID(), "Pressure after compressor", "pressure"),
                        new ValueConfig(UUID.randomUUID(), "Temperature before compressor", "temperature"),
                        new ValueConfig(UUID.randomUUID(), "Temperature in combustion chamber", "temperature"),
                        new ValueConfig(UUID.randomUUID(), "Input flow rate", "flowRate"),
                        new ValueConfig(UUID.randomUUID(), "Output flow rate", "flowRate"),
                        new ValueConfig(UUID.randomUUID(), "Input gas composition", "composition")
                )))
                .averageSensorValuesConfig(new ArrayList<>(List.of(
                        new ValueConfig(UUID.randomUUID(), "Pressure after compressor", "pressure"),
                        new ValueConfig(UUID.randomUUID(), "Temperature before compressor", "temperature"),
                        new ValueConfig(UUID.randomUUID(), "Temperature in combustion chamber", "temperature"),
                        new ValueConfig(UUID.randomUUID(), "Input flow rate", "flowRate"),
                        new ValueConfig(UUID.randomUUID(), "Output flow rate", "flowRate"),
                        new ValueConfig(UUID.randomUUID(), "Input gas composition", "composition")
                )))
                .chartConfigs(new ArrayList<>(List.of(
                        new ChartConfig(UUID.randomUUID(), "Temperature before compressor", "temperature") ,
                        new ChartConfig(UUID.randomUUID(), "Pressure after compressor", "pressure") ,
                        new ChartConfig(UUID.randomUUID(), "Flow rate", "flowRate")
                )));
        dashboardConfigs.put(DASHBOARD_CONFIG_ID, config);
    }

    @Override
    @SneakyThrows
    public ResponseEntity<DashboardConfig> getDashboardConfig(String userName) {
    Thread.sleep(500);
        return ResponseEntity.ok(this.dashboardConfigs.get(userName));
    }

    @Override
    public ResponseEntity<DashboardConfig> updateDashboardConfig(String userName, @Valid DashboardConfig dashboardConfig) {

        dashboardConfig.getChartConfigs().forEach(c -> {
            if (c.getId() == null) {
                c.setId(UUID.randomUUID());
            }
        });

        dashboardConfig.getCurrentSensorValuesConfig().forEach(c -> {
            if (c.getId() == null) {
                c.setId(UUID.randomUUID());
            }
        });

        dashboardConfig.getAverageSensorValuesConfig().forEach(c -> {
            if (c.getId() == null) {
                c.setId(UUID.randomUUID());
            }
        });

        this.dashboardConfigs.put(userName, dashboardConfig);
        return ResponseEntity.ok(this.dashboardConfigs.get(userName));
    }

    private List<SensorData> generateData(String sensorConfigId, int days) {
        var sensorType = this.dashboardConfigs.get(DASHBOARD_CONFIG_ID)
                .getChartConfigs()
                .stream()
                .filter(c -> c.getId().equals(UUID.fromString(sensorConfigId)))
                .findFirst().get().getSensorType();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(days);
        var data = new ArrayList<SensorData>();
        Random random = new Random();

        double min, max;
        switch (sensorType) {
            case "temperature":
                min = 5;
                max = 40;
                break;
            case "pressure":
                min = 2;
                max = 8;
                break;
            case "flowRate":
                min = 0;
                max = 300;
                break;
            case "composition":
                min = 0;
                max = 1;
                break;
            case "compressorState":
                min = 0;
                max = 1;
                break;
            default:
                min = 0;
                max = 100;
        }

        while (startDate.isBefore(now) || startDate.isEqual(now)) {
            Map<String, BigDecimal> values = generateValues(sensorType, random, max, min);

            data.add(new SensorData()
                    .timestamp(startDate.toInstant(ZoneOffset.UTC).toEpochMilli())
                    .values(values)
            );
            startDate = startDate.plusMinutes(15);
        }

        return data;
    }

    public static Map<String, BigDecimal> generateValues(String sensorType, Random random, double max, double min) {
        return switch (sensorType) {
            case "composition" -> Map.of(
                    "CO2", BigDecimal.valueOf(random.nextDouble() * (max - min) + min),
                    "H2O", BigDecimal.valueOf(random.nextDouble()),
                    "O2", BigDecimal.valueOf(random.nextDouble()),
                    "N2", BigDecimal.valueOf(random.nextDouble()),
                    "NH3", BigDecimal.valueOf(random.nextDouble())
            );
            case "compressorState" -> Map.of(
                    "amplitude", BigDecimal.valueOf(random.nextDouble()),
                    "frequency", BigDecimal.valueOf(random.nextDouble()),
                    "level", BigDecimal.valueOf(random.nextDouble())
            );
            case "temperature" -> Map.of(
                    "temperature", BigDecimal.valueOf(random.nextDouble())
            );
            case "pressure" -> Map.of(
                    "pressure", BigDecimal.valueOf(random.nextDouble())
            );
            case "flowRate" -> Map.of(
                    "flowRate", BigDecimal.valueOf(random.nextDouble())
            );
            default -> Map.of();
        };
    }

    private int getDaysFromTimeRange(String timeRange) {
        return switch (timeRange) {
            case "twoDays" -> 2;
            case "threeDays" -> 3;
            case "fiveDays" -> 5;
            case "week" -> 7;
            default -> 1;
        };
    }

    @Override
    public ResponseEntity<List<SensorValue>> getHomeAverageSensorValues(String userName) {
        List<SensorValue> sensorValues = dashboardConfigs.get(userName)
                .getAverageSensorValuesConfig().stream()
                .map(c -> SensorValue.builder()
                        .id(c.getId())
                        .values(generateSensorData(c.getSensorType()))
                        .sensorType(c.getSensorType())
                        .label(c.getLabel())
                        .build())
                .toList();
        return ResponseEntity.ok(sensorValues);
    }

    @Override
    public ResponseEntity<Map<String, List<SensorData>>> getHomeChartData(@NotNull @Valid List<String> chartConfigIds, @NotNull @Valid String timeRange) {
        int days = getDaysFromTimeRange(timeRange);
        Map<String, List<SensorData>> data = chartConfigIds.stream()
                .collect(Collectors.toMap(Function.identity(), id -> generateData(id, days)));

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
    public ResponseEntity<List<SensorValue>> getHomeSensorValues(String userName) {

        List<SensorValue> sensorValues = dashboardConfigs.get(userName)
                .getCurrentSensorValuesConfig().stream()
                .map(c -> SensorValue.builder()
                        .id(c.getId())
                        .values(generateSensorData(c.getSensorType()))
                        .sensorType(c.getSensorType())
                        .label(c.getLabel())
                        .build())
                .toList();
        return ResponseEntity.ok(sensorValues);
    }

    private Map<String, BigDecimal> generateSensorData(String sensorType) {
        Random random = new Random();

        double min, max;
        switch (sensorType) {
            case "temperature":
                min = 5;
                max = 40;
                break;
            case "pressure":
                min = 2;
                max = 8;
                break;
            case "flowRate":
                min = 0;
                max = 300;
                break;
            case "composition":
                min = 0;
                max = 1;
                break;
            case "compressorState":
                min = 0;
                max = 1;
                break;
            default:
                min = 0;
                max = 100;
        }

        return generateValues(sensorType, random, max, min);
    }

}
