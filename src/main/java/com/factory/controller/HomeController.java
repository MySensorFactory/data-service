package com.factory.controller;

import com.factory.domain.BasicSensorDataEntry;
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
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    public ResponseEntity<Map<String, List<SensorData>>> getHomeChartData(List<String> chartConfigIds, String timeRange) {
//        Map<String, List<SensorData>> result =
//                sensorsService.getSensorsData(timeRange, timeRange);
//        var config = chartConfigRepository.findAllById(chartConfigIds.stream().map(UUID::fromString).toList());
//
//        config.forEach( c -> {
//            result.put(c.getSensorType(), sensorsService.)
//        });
//
//        return ResponseEntity.ok(result);
        return null;
    }

    @Override
    public ResponseEntity<List<Event>> getHomeEvents(Boolean showOnlyAlerts, String searchTerm, LocalDate startDate, LocalDate endDate) {
        return null;
    }

}
