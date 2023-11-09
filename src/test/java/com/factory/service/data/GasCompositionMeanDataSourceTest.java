package com.factory.service.data;

import com.factory.config.DataSourceConfig;
import com.factory.config.dto.DataSource;
import com.factory.domain.SensorData;
import com.factory.domain.SensorLabel;
import com.factory.persistence.data.entity.AuditData;
import com.factory.persistence.data.entity.MeanGasComposition;
import com.factory.persistence.data.repository.MeanGasCompositionRepository;
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

class GasCompositionMeanDataSourceTest {

    public static final String GAS_COMPOSITION_MEAN = "gasCompositionMean";
    public static final String TEST_LABEL = "testLabel";

    @Mock
    private DataSourceConfig dataSourceConfig;

    @Mock
    private MeanGasCompositionRepository meanGasCompositionRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private GasCompositionMeanDataSource gasCompositionMeanDataSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByLabelAndTimeWindow() {
        SensorLabel label = SensorLabel.of(TEST_LABEL);
        ZonedDateTime from = ZonedDateTime.now().minusHours(1);
        ZonedDateTime to = ZonedDateTime.now();

        MeanGasComposition gasCompositionMean = new MeanGasComposition();
        gasCompositionMean.setId(UUID.randomUUID());
        gasCompositionMean.setAuditData(new AuditData());
        gasCompositionMean.setCo2(1.0);
        gasCompositionMean.setH2(2.0);
        gasCompositionMean.setNh3(3.0);
        gasCompositionMean.setN2(4.0);
        gasCompositionMean.setO2(5.0);

        List<MeanGasComposition> gasCompositionMeanList = Collections.singletonList(gasCompositionMean);

        when(meanGasCompositionRepository.findByTimeWindowAndLabel(label.getLabel(), from, to)).thenReturn(gasCompositionMeanList);
        when(dataSourceConfig.getDataSources()).thenReturn(Collections.singletonMap(GAS_COMPOSITION_MEAN, createTestDataSource()));

        SensorData result = gasCompositionMeanDataSource.findByLabelAndTimeWindow(label, from, to);

        assertEquals(GAS_COMPOSITION_MEAN, result.getSensorType());
        assertEquals(label.getLabel(), result.getLabel());
        assertEquals(gasCompositionMeanList.size(), result.getEntries().size());

        verify(meanGasCompositionRepository, times(1)).findByTimeWindowAndLabel(label.getLabel(), from, to);
        verify(modelMapper, times(3)).map(any(), eq(Long.class));
    }

    private DataSource createTestDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setSensorType(GAS_COMPOSITION_MEAN);
        dataSource.setAvailableLabels(Collections.singletonList(TEST_LABEL));
        return dataSource;
    }
}
