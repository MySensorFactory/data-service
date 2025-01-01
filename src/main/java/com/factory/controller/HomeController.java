package com.factory.controller;

import com.factory.config.dto.DataConfig;
import com.factory.domain.SensorDataEntry;
import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;
import com.factory.mapping.HomeMapper;
import com.factory.openapi.api.HomeApi;
import com.factory.openapi.model.DashboardConfig;
import com.factory.openapi.model.Event;
import com.factory.openapi.model.SensorData;
import com.factory.openapi.model.SensorValue;
import com.factory.persistence.home.entity.ValueConfig;
import com.factory.persistence.home.repository.ChartConfigRepository;
import com.factory.persistence.home.repository.DashboardsConfigRepository;
import com.factory.service.SensorsService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class HomeController implements HomeApi {

    private final DashboardsConfigRepository dashboardsConfigRepository;

    private final ChartConfigRepository chartConfigRepository;

    private final HomeMapper homeMapper;

    private final SensorsService sensorsService;

    private final DataConfig dataConfig;

    @Override
    public ResponseEntity<DashboardConfig> getDashboardConfig(String userName) {
        var result = homeMapper.map(dashboardsConfigRepository.findByUserName(userName));
        return ResponseEntity.ok(result);
    }

    @Override
    @Transactional
    public ResponseEntity<DashboardConfig> updateDashboardConfig(String userName, final DashboardConfig dashboardConfig) {
        var config = dashboardsConfigRepository.findByUserName(userName);

        config.getValueConfigs().clear();
        config.getChartConfigs().clear();

        config.getValueConfigs().addAll(homeMapper.mapAverageSensorValuesToEntities(config, dashboardConfig.getAverageSensorValuesConfig()));
        config.getValueConfigs().addAll(homeMapper.mapCurrentSensorValuesToEntities(config, dashboardConfig.getCurrentSensorValuesConfig()));
        config.setChartConfigs(homeMapper.map(config, dashboardConfig.getChartConfigs()));

        dashboardsConfigRepository.save(config);

        return ResponseEntity.ok(homeMapper.map(config));
    }

    @Override
    public ResponseEntity<List<SensorValue>> getHomeAverageSensorValues(String userName) {
        com.factory.persistence.home.entity.DashboardConfig config = dashboardsConfigRepository.findByUserName(userName);
        List<ValueConfig> valueConfigs = config.getValueConfigs().stream().filter(vc -> !vc.isCurrent()).toList();
        Set<SensorDataEntry> result = sensorsService.getLatestAverageSensorData(valueConfigs.stream().map(valueConfig -> Pair.of(
                SensorLabel.of(valueConfig.getLabel()),
                SensorType.of(valueConfig.getSensorType())
        )).collect(Collectors.toSet()));

        return ResponseEntity.ok(homeMapper.mapSensorValues(result));
    }

    @Override
    public ResponseEntity<List<SensorValue>> getHomeSensorValues(String userName) {
        com.factory.persistence.home.entity.DashboardConfig config = dashboardsConfigRepository.findByUserName(userName);
        List<ValueConfig> valueConfigs = config.getValueConfigs().stream().filter(ValueConfig::isCurrent).toList();
        Set<SensorDataEntry> result = sensorsService.getLatestCurrentSensorData(valueConfigs.stream().map(valueConfig -> Pair.of(
                SensorLabel.of(valueConfig.getLabel()),
                SensorType.of(valueConfig.getSensorType())
        )).collect(Collectors.toSet()));

        return ResponseEntity.ok(homeMapper.mapSensorValues(result));
    }

    @Override
    public ResponseEntity<Map<String, List<SensorData>>> getHomeChartData(final List<String> chartConfigIds, String timeRange) {
        long lastDays = dataConfig.getTimeRangeOptions().stream()
                .filter(tr -> tr.getValue().equals(timeRange))
                .findFirst().orElseThrow()
                .getDaysCount();

        var configs = chartConfigRepository.findAllById(chartConfigIds.stream()
                .map(UUID::fromString)
                .toList());

        Map<String, List<SensorData>> result = configs.stream().map(config ->
                        sensorsService.getSensorsData(
                                ZonedDateTime.now().minusDays(lastDays),
                                ZonedDateTime.now(),
                                SensorLabel.of(config.getLabel()),
                                Set.of(SensorType.of(config.getSensorType()))
                        ))
                .flatMap(map -> map.entrySet()
                        .stream())
                .collect(Collectors.toMap(
                        e -> e.getKey().getType(),
                        e -> homeMapper.mapBasicSensorValues(e.getValue())
                ));

        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<List<Event>> getHomeEvents(final Boolean showOnlyAlerts,
                                                     String searchTerm,
                                                     final LocalDate startDate,
                                                     final LocalDate endDate) {
        return null;
    }

}
