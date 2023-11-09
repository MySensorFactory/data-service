package com.factory.service.data;

import com.factory.config.DataSourceConfig;
import com.factory.config.dto.DataSource;
import com.factory.domain.SensorData;
import com.factory.domain.SensorLabel;
import com.factory.persistence.data.entity.AuditData;
import com.factory.persistence.data.entity.CompressorState;
import com.factory.persistence.data.repository.CompressorStateRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompressorStateDataSourceTest {

    @Mock
    private DataSourceConfig dataSourceConfig;

    @Mock
    private CompressorStateRepository compressorStateRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CompressorStateDataSource compressorStateDataSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByLabelAndTimeWindow() {
        SensorLabel label = SensorLabel.of("testLabel");
        ZonedDateTime from = ZonedDateTime.now().minusHours(1);
        ZonedDateTime to = ZonedDateTime.now();

        CompressorState compressorState = new CompressorState();
        compressorState.setId(UUID.randomUUID());
        compressorState.setAuditData(new AuditData());
        compressorState.setNoiseLevel(1.0);
        compressorState.setVibrationAmplitude(2.0);
        compressorState.setVibrationFrequency(3.0);

        List<CompressorState> compressorStateList = Collections.singletonList(compressorState);

        when(compressorStateRepository.findByTimeWindowAndLabel(label.getLabel(), from, to)).thenReturn(compressorStateList);
        when(dataSourceConfig.getDataSources()).thenReturn(Collections.singletonMap("compressorState", createTestDataSource()));

        SensorData result = compressorStateDataSource.findByLabelAndTimeWindow(label, from, to);

        assertEquals("compressorState", result.getSensorType());
        assertEquals(label.getLabel(), result.getLabel());
        assertEquals(compressorStateList.size(), result.getEntries().size());

        verify(compressorStateRepository, times(1)).findByTimeWindowAndLabel(label.getLabel(), from, to);
        verify(modelMapper, times(3)).map(any(), eq(Long.class));
    }

    private DataSource createTestDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setSensorType("compressorState");
        dataSource.setAvailableLabels(Collections.singletonList("testLabel"));
        return dataSource;
    }
}
