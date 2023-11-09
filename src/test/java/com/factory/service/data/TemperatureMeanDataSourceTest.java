package com.factory.service.data;

import com.factory.config.DataSourceConfig;
import com.factory.config.dto.DataSource;
import com.factory.domain.SensorData;
import com.factory.domain.SensorLabel;
import com.factory.persistence.data.entity.AuditData;
import com.factory.persistence.data.entity.MeanTemperature;
import com.factory.persistence.data.repository.MeanTemperatureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TemperatureMeanDataSourceTest {

    public static final String TEMPERATURE_MEAN = "temperatureMean";
    public static final String TEST_LABEL = "testLabel";

    @Mock
    private DataSourceConfig dataSourceConfig;

    @Mock
    private MeanTemperatureRepository meanTemperatureRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TemperatureMeanDataSource temperatureMeanDataSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByLabelAndTimeWindow() {
        SensorLabel label = SensorLabel.of(TEST_LABEL);
        ZonedDateTime from = ZonedDateTime.now().minusHours(1);
        ZonedDateTime to = ZonedDateTime.now();

        MeanTemperature temperatureMean = new MeanTemperature();
        temperatureMean.setId(UUID.randomUUID());
        temperatureMean.setAuditData(new AuditData());
        temperatureMean.setValue(42.0);

        List<MeanTemperature> temperatureMeanList = Collections.singletonList(temperatureMean);

        when(meanTemperatureRepository.findByTimeWindowAndLabel(label.getLabel(), from, to)).thenReturn(temperatureMeanList);
        when(dataSourceConfig.getDataSources()).thenReturn(Collections.singletonMap(TEMPERATURE_MEAN, createTestDataSource()));

        SensorData result = temperatureMeanDataSource.findByLabelAndTimeWindow(label, from, to);

        assertEquals(TEMPERATURE_MEAN, result.getSensorType());
        assertEquals(label.getLabel(), result.getLabel());
        assertEquals(temperatureMeanList.size(), result.getEntries().size());

        verify(meanTemperatureRepository, times(1)).findByTimeWindowAndLabel(label.getLabel(), from, to);
        verify(modelMapper, times(3)).map(any(), eq(Long.class));
    }

    private DataSource createTestDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setSensorType(TEMPERATURE_MEAN);
        dataSource.setAvailableLabels(Collections.singletonList(TEST_LABEL));
        return dataSource;
    }
}
