package com.factory.service;

import com.factory.domain.EventKey;
import com.factory.domain.ReportData;
import com.factory.domain.SensorData;
import com.factory.domain.SensorDataEntry;
import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;
import com.factory.service.data.SensorDataSourceResolver;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultSensorsService implements SensorsService {

    private final SensorDataSourceResolver sensorDataSourceResolver;
    private final ModelMapper modelMapper;

    @Override
    public ReportData getSensorsData(final ZonedDateTime from,
                                     final ZonedDateTime to,
                                     final Map<SensorType, SensorLabel> sensorLabels) {

        if (CollectionUtils.isEmpty(sensorLabels)) {
            return getEmptyReport();
        }

        var input = findWindowedData(from, to, sensorLabels);

        if (CollectionUtils.isEmpty(input)) {
            return getEmptyReport();
        }

        var result = initResult(sensorLabels);
        var singleDataSet = getAllDataFromRandomSensorType(input, sensorLabels);

        if (CollectionUtils.isEmpty(singleDataSet)) {
            return getEmptyReport();
        }

        copySingleDataSet(singleDataSet).stream()
                .map(Map.Entry::getKey)
                .filter(eventKey -> isEventKeyPresentInAllSensorEntityData(input, eventKey))
                .forEach(eventKey -> moveSensorDataEntriesWithGivenEventKeyFromInputToResult(sensorLabels, input, result, eventKey));

        return result;
    }

    private void moveSensorDataEntriesWithGivenEventKeyFromInputToResult(final Map<SensorType, SensorLabel> sensorLabels,
                                                                         final Map<SensorType, SensorData> data,
                                                                         final ReportData result,
                                                                         final EventKey eventKey) {
        sensorLabels.forEach((key, value) -> {
            result.getSensorsInstantEntries().get(key).put(eventKey, data.get(key).getEntries().get(eventKey));
            data.get(key).getEntries().remove(eventKey);
        });
    }

    private HashSet<Map.Entry<EventKey, SensorDataEntry>> copySingleDataSet(final Map<EventKey, SensorDataEntry> singleDataSet) {
        return new HashSet<>(singleDataSet.entrySet());
    }

    private Map<EventKey, SensorDataEntry> getAllDataFromRandomSensorType(final Map<SensorType, SensorData> data,
                                                                          final Map<SensorType, SensorLabel> sensorLabels) {
        var randomSensorLabel = getRandomSensorLabel(sensorLabels);
        return data.get(randomSensorLabel.getKey()).getEntries();
    }

    private Map.Entry<SensorType, SensorLabel> getRandomSensorLabel(final Map<SensorType, SensorLabel> sensorLabels) {
        return sensorLabels.entrySet().stream().findFirst().orElseThrow();
    }

    private ReportData initResult(final Map<SensorType, SensorLabel> sensorLabels) {
        return ReportData.builder()
                .sensorsInstantEntries(sensorLabels
                        .entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                entry -> new HashMap<>())))
                .build();
    }

    private ReportData getEmptyReport() {
        return ReportData.builder()
                .sensorsInstantEntries(Map.of())
                .build();
    }

    private Map<SensorType, SensorData> findWindowedData(final ZonedDateTime from,
                                                         final ZonedDateTime to,
                                                         final Map<SensorType, SensorLabel> sensorLabels) {
        return sensorLabels.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> sensorDataSourceResolver.getDataSource(entry.getKey())
                                .findByLabelAndTimeWindow(entry.getValue(), from, to))
                );
    }

    @Override
    public SensorData getSingleReports(final Long from,
                                       final Long to,
                                       final SensorLabel label,
                                       final SensorType sensorType) {
        return sensorDataSourceResolver.getDataSource(sensorType).findByLabelAndTimeWindow(label,
                modelMapper.map(from, ZonedDateTime.class),
                modelMapper.map(to, ZonedDateTime.class)
        );
    }

    private boolean isEventKeyPresentInAllSensorEntityData(final Map<SensorType, SensorData> data,
                                                           final EventKey eventKey) {
        final AtomicReference<Boolean> result = new AtomicReference<>(true);
        data.forEach((key, value) -> {
            if (!value.getEntries().containsKey(eventKey)) {
                result.set(false);
            }
        });
        return result.get();
    }
}
