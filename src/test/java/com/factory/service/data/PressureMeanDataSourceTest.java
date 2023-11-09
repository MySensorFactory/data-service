package com.factory.service.data;

import com.factory.config.DataSourceConfig;
import com.factory.config.dto.DataSource;
import com.factory.domain.SensorData;
import com.factory.domain.SensorLabel;
import com.factory.persistence.data.entity.AuditData;
import com.factory.persistence.data.entity.MeanPressure;
import com.factory.persistence.data.repository.MeanPressureRepository;
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

class PressureMeanDataSourceTest {

    public static final String PRESSURE_MEAN = "pressureMean";
    public static final String TEST_LABEL = "testLabel";

    @Mock
    private DataSourceConfig dataSourceConfig;

    @Mock
    private MeanPressureRepository meanPressureRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PressureMeanDataSource pressureMeanDataSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByLabelAndTimeWindow() {
        SensorLabel label = SensorLabel.of(TEST_LABEL);
        ZonedDateTime from = ZonedDateTime.now().minusHours(1);
        ZonedDateTime to = ZonedDateTime.now();

        MeanPressure pressureMean = new MeanPressure();
        pressureMean.setId(UUID.randomUUID());
        pressureMean.setAuditData(new AuditData());
        pressureMean.setValue(42.0);

        List<MeanPressure> pressureMeanList = Collections.singletonList(pressureMean);

        when(meanPressureRepository.findByTimeWindowAndLabel(label.getLabel(), from, to)).thenReturn(pressureMeanList);
        when(dataSourceConfig.getDataSources()).thenReturn(Collections.singletonMap(PRESSURE_MEAN, createTestDataSource()));

        SensorData result = pressureMeanDataSource.findByLabelAndTimeWindow(label, from, to);

        assertEquals(PRESSURE_MEAN, result.getSensorType());
        assertEquals(label.getLabel(), result.getLabel());
        assertEquals(pressureMeanList.size(), result.getEntries().size());

        verify(meanPressureRepository, times(1)).findByTimeWindowAndLabel(label.getLabel(), from, to);
        verify(modelMapper, times(3)).map(any(), eq(Long.class));
    }

    private DataSource createTestDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setSensorType(PRESSURE_MEAN);
        dataSource.setAvailableLabels(Collections.singletonList(TEST_LABEL));
        return dataSource;
    }
}
