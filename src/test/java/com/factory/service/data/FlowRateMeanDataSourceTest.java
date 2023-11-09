package com.factory.service.data;

import com.factory.config.DataSourceConfig;
import com.factory.config.dto.DataSource;
import com.factory.domain.SensorData;
import com.factory.domain.SensorLabel;
import com.factory.persistence.data.entity.AuditData;
import com.factory.persistence.data.entity.MeanFlowRate;
import com.factory.persistence.data.repository.MeanFlowRateRepository;
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

class FlowRateMeanDataSourceTest {

    public static final String FLOW_RATE_MEAN = "flowRateMean";
    public static final String TEST_LABEL = "testLabel";
    @Mock
    private DataSourceConfig dataSourceConfig;

    @Mock
    private MeanFlowRateRepository flowRateMeanRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private FlowRateMeanDataSource flowRateMeanDataSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByLabelAndTimeWindow() {
        SensorLabel label = SensorLabel.of(TEST_LABEL);
        ZonedDateTime from = ZonedDateTime.now().minusHours(1);
        ZonedDateTime to = ZonedDateTime.now();

        MeanFlowRate flowRate = new MeanFlowRate();
        flowRate. setId(UUID.randomUUID());
        flowRate.setAuditData(new AuditData());
        flowRate.setValue(42.0);

        List<MeanFlowRate> flowRateMeanList = Collections.singletonList(flowRate);

        when(flowRateMeanRepository.findByTimeWindowAndLabel(label.getLabel(), from, to)).thenReturn(flowRateMeanList);
        when(dataSourceConfig.getDataSources()).thenReturn(Collections.singletonMap(FLOW_RATE_MEAN, createTestDataSource()));

        SensorData result = flowRateMeanDataSource.findByLabelAndTimeWindow(label, from, to);

        assertEquals(FLOW_RATE_MEAN, result.getSensorType());
        assertEquals(label.getLabel(), result.getLabel());
        assertEquals(flowRateMeanList.size(), result.getEntries().size());

        verify(flowRateMeanRepository, times(1)).findByTimeWindowAndLabel(label.getLabel(), from, to);
        verify(modelMapper, times(3)).map(any(), eq(Long.class));
    }

    private DataSource createTestDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setSensorType(FLOW_RATE_MEAN);
        dataSource.setAvailableLabels(Collections.singletonList(TEST_LABEL));
        return dataSource;
    }
}
