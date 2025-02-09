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
import com.factory.persistence.home.entity.ChartConfig;
import com.factory.persistence.home.entity.ValueConfig;
import com.factory.persistence.home.repository.ChartConfigRepository;
import com.factory.persistence.home.repository.DashboardsConfigRepository;
import com.factory.persistence.home.repository.EventsRepository;
import com.factory.persistence.home.repository.ValueConfigRepository;
import com.factory.service.SensorsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HomeController implements HomeApi {

    private final DashboardsConfigRepository dashboardsConfigRepository;

    private final ChartConfigRepository chartConfigRepository;

    private final ValueConfigRepository valueConfigRepository;

    private final HomeMapper homeMapper;

    private final SensorsService sensorsService;

    private final DataConfig dataConfig;

    private final EventsRepository eventsRepository;

    @Override
    public ResponseEntity<DashboardConfig> getDashboardConfig(String userName) {
        var result = homeMapper.map(dashboardsConfigRepository.findByUserName(userName));

        if (result == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(result);
    }

    @Override
    @Transactional
    public ResponseEntity<DashboardConfig> updateDashboardConfig(String userName, final DashboardConfig dashboardConfig) {
        var config = dashboardsConfigRepository.findByUserName(userName);

        log.info("111");
        chartConfigRepository.deleteAllById(config.getChartConfigs().stream().map(ChartConfig::getId).toList());
//        valueConfigRepository.deleteAllById(config.getValueConfigs().stream().map(ValueConfig::getId).toList());

        log.info("222");
        valueConfigRepository.deleteAllByDashboardConfig(config);
        valueConfigRepository.flush();

        config.clearChartConfigs();
        config.clearValueConfigs();
//        chartConfigRepository.flush();
//        valueConfigRepository.flush();

        log.info("333");
        dashboardsConfigRepository.flush();
        config = dashboardsConfigRepository.findByUserName(userName);

        config.getValueConfigs().addAll(homeMapper.mapAverageSensorValuesToEntities(config, dashboardConfig.getAverageSensorValuesConfig()));
        config.getValueConfigs().addAll(homeMapper.mapCurrentSensorValuesToEntities(config, dashboardConfig.getCurrentSensorValuesConfig()));
        config.getChartConfigs().addAll(homeMapper.map(config, dashboardConfig.getChartConfigs()));

        dashboardsConfigRepository.save(config);

        return ResponseEntity.ok(homeMapper.map(config));
    }

    //TODO: fix
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

    //TODO: fix
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

        Map<String, List<SensorData>> result = configs.stream()
                .collect(Collectors.toMap(
                        chartConfig -> chartConfig.getId().toString(),
                        chartConfig -> {
                            var toMap = sensorsService.getSensorsData(
                                    ZonedDateTime.now().minusDays(lastDays),
                                    ZonedDateTime.now(),
                                    SensorLabel.of(chartConfig.getLabel()),
                                    Set.of(SensorType.of(chartConfig.getSensorType()))
                            )
                                    .entrySet().stream()
                                    .findFirst().get()
                                    .getValue();
                            return homeMapper.mapBasicSensorValues(toMap);
                        }
                ));

        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<List<Event>> getHomeEvents(final Boolean showOnlyAlerts,
                                                     String searchTerm,
                                                     final LocalDate startDate,
                                                     final LocalDate endDate) {
        if (showOnlyAlerts) {
            return ResponseEntity.ok(eventsRepository.findAllByTitleContainsAndTimestampBetweenAndIsAlertTrue(
                            searchTerm,
                            startDate != null ? startDate.atStartOfDay(ZoneId.systemDefault()) : LocalDate.now().minusWeeks(1).atStartOfDay(ZoneId.systemDefault()),
                            endDate != null ? endDate.atStartOfDay(ZoneId.systemDefault()) : LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                    )
                    .stream()
                    .map(homeMapper::map)
                    .map(homeMapper::map)
                    .toList());
        }

        return ResponseEntity.ok(eventsRepository.findAllByTitleContainsAndTimestampBetween(
                        searchTerm,
                        startDate != null ? startDate.atStartOfDay(ZoneId.systemDefault()) : LocalDate.now().minusWeeks(1).atStartOfDay(ZoneId.systemDefault()),
                        endDate != null ? endDate.atStartOfDay(ZoneId.systemDefault()) : LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                )
                .stream()
                .map(homeMapper::map)
                .map(homeMapper::map)
                .toList());
    }

}
