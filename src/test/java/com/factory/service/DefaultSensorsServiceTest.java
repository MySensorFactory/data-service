package com.factory.service;

import com.factory.domain.EventKey;
import com.factory.domain.ReportData;
import com.factory.domain.SensorData;
import com.factory.domain.SensorDataEntry;
import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;
import com.factory.service.data.SensorDataSource;
import com.factory.service.data.SensorDataSourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultSensorsServiceTest {

    @Mock
    private SensorDataSourceResolver sensorDataSourceResolver;

    @InjectMocks
    private DefaultSensorsService sensorsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getSensorsDataNoWindowedDataShouldReturnEmptyReportData() {
        // Arrange
        Map<SensorType, SensorLabel> sensorLabels = Collections.emptyMap();
        when(sensorDataSourceResolver.getDataSource(any(SensorType.class)))
                .thenReturn(mock(SensorDataSource.class));

        // Act
        ReportData result = sensorsService.getSensorsData(ZonedDateTime.now(), ZonedDateTime.now(), sensorLabels);

        // Assert
        assertEquals(0, result.getSensorsInstantEntries().size());
    }

    @Test
    void getSensorsDataSuccessfulExecutionShouldReturnReportData() {
        // Arrange
        Map<SensorType, SensorLabel> sensorLabels = createSensorLabels();
        Map<SensorType, SensorDataSource> dataSources = createDataSources(sensorLabels);
        Map<EventKey, SensorDataEntry> sensorDataEntries = createSensorDataEntries();

        when(sensorDataSourceResolver.getDataSource(any(SensorType.class)))
                .thenAnswer(invocation -> {
                    SensorType type = invocation.getArgument(0);
                    SensorDataSource dataSource = dataSources.get(type);
                    when(dataSource.findByLabelAndTimeWindow(any(SensorLabel.class), any(ZonedDateTime.class), any(ZonedDateTime.class)))
                            .thenReturn(SensorData.builder().entries(sensorDataEntries).build());
                    return dataSource;
                });

        // Act
        ReportData result = sensorsService.getSensorsData(ZonedDateTime.now(), ZonedDateTime.now(), sensorLabels);

        // Assert
        assertEquals(1, result.getSensorsInstantEntries().size());
        assertEquals(createSensorDataEntries(), result.getSensorsInstantEntries().get(sensorLabels.keySet().iterator().next()));
    }

    private Map<SensorType, SensorLabel> createSensorLabels() {
        return Collections.singletonMap(SensorType.builder()
                        .type("type")
                        .build(),
                SensorLabel.builder()
                        .label("label")
                        .build());
    }

    private Map<SensorType, SensorDataSource> createDataSources(Map<SensorType, SensorLabel> sensorLabels) {
        Map<SensorType, SensorDataSource> dataSources = new HashMap<>();
        for (SensorType type : sensorLabels.keySet()) {
            dataSources.put(type, mock(SensorDataSource.class));
        }
        return dataSources;
    }

    private Map<EventKey, SensorDataEntry> createSensorDataEntries() {
        EventKey eventKey = EventKey.builder().key("key").build();
        SensorDataEntry sensorDataEntry = SensorDataEntry.builder()
                .timestamp(123456L)
                .label("label")
                .eventKey("eventKey")
                .sensorType("sensorType")
                .data(new HashMap<>(Collections.singletonMap("key", 42.0)))
                .build();

        Map<EventKey, SensorDataEntry> result = new HashMap<>();
        result.put(eventKey, sensorDataEntry);
        return result;
    }

}


