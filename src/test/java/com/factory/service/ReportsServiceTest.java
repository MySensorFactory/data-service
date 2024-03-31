package com.factory.service;

import com.factory.domain.ReportData;
import com.factory.domain.SensorData;
import com.factory.exception.ClientErrorException;
import com.factory.mapping.CollectionMapper;
import com.factory.openapi.model.*;
import com.factory.persistence.data.entity.Report;
import com.factory.persistence.data.repository.ReportsRepository;
import com.factory.persistence.elasticsearch.model.ReportDataEsModel;
import com.factory.persistence.elasticsearch.repository.ReportsEsRepository;
import com.factory.validation.SensorTypeLabelsValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportsServiceTest {

    @Mock
    private ReportsRepository reportsRepository;

    @Mock
    private SensorsService sensorsService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CollectionMapper collectionMapper;

    @Mock
    private SensorTypeLabelsValidator sensorTypeLabelsValidator;

    @Mock
    private ReportsEsRepository reportsEsRepository;

    @InjectMocks
    private ReportsService reportsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createReportsValidRequestShouldReturnCreateReportResponse() {
        // Arrange
        var request = new UpsertReportRequest();
        when(collectionMapper.stringMapToSensorTypeLabelMap(any())).thenReturn(Collections.emptyMap());
        when(modelMapper.map(any(), eq(Report.class))).thenReturn(getReport());
        when(reportsRepository.save(any())).thenReturn(getReport());
        when(modelMapper.map(any(), eq(ReportDataEsModel.class))).thenReturn(new ReportDataEsModel());
        when(modelMapper.map(any(), eq(UpsertReportResponse.class))).thenReturn(new UpsertReportResponse());

        // Act
        var response = reportsService.createReports(request);

        // Assert
        assertNotNull(response);
        verify(sensorTypeLabelsValidator).validate(any());
        verify(collectionMapper).stringMapToSensorTypeLabelMap(any());
        verify(modelMapper).map(any(), eq(Report.class));
        verify(reportsRepository).save(any());
    }

    @Test
    void getReportDetailsExistingReportIdShouldReturnGetReportDetailsResponse() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = getReport();
        report.setReportSensorLabels(Set.of());
        report.setLabel("lbl");
        report.setTo(ZonedDateTime.now().plusDays(1));
        report.setFrom(ZonedDateTime.now().minusDays(1));
        when(reportsRepository.findById(reportId)).thenReturn(java.util.Optional.of(report));
        when(sensorsService.getSensorsData(any(), any(), any())).thenReturn(createMockReportData());
        when(modelMapper.map(any(), eq(GetReportDetailsResponse.class))).thenReturn(new GetReportDetailsResponse());

        // Act
        GetReportDetailsResponse response = reportsService.getReportDetails(reportId);

        // Assert
        assertNotNull(response);
        verify(reportsRepository).findById(reportId);
        verify(sensorsService).getSensorsData(any(), any(), any());
        verify(modelMapper).map(any(), eq(GetReportDetailsResponse.class));
    }

    @Test
    void getReportDetailsNonExistingReportIdShouldThrowClientErrorException() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        when(reportsRepository.findById(reportId)).thenReturn(java.util.Optional.empty());

        // Act and Assert
        assertThrows(ClientErrorException.class, () -> reportsService.getReportDetails(reportId));
        verify(reportsRepository).findById(reportId);
    }

    @Test
    void getReportsListValidTimeRangeShouldReturnGetReportListResponse() {
        // Arrange
        Long from = System.currentTimeMillis();
        Long to = System.currentTimeMillis() + 1000;
        List<Report> reports = Collections.singletonList(getReport());
        when(reportsRepository.findAllByTimeWindow(any(), any())).thenReturn(reports);
        when(collectionMapper.reportListToDto(anyList())).thenReturn(new GetReportListResponse());

        // Act
        GetReportListResponse response = reportsService.getReportsList(from, to);

        // Assert
        assertNotNull(response);
        verify(reportsRepository).findAllByTimeWindow(any(), any());
        verify(collectionMapper).reportListToDto(anyList());
    }

    @Test
    void getSingleReportsValidParametersShouldReturnGetSingleReportResponse() {
        // Arrange
        Long from = System.currentTimeMillis();
        Long to = System.currentTimeMillis() + 1000;
        String label = "Label";
        String sensorType = "SensorType";
        when(sensorsService.getSingleReports(any(), any(), any(), any())).thenReturn(createMockSensorData());
        when(modelMapper.map(any(), eq(GetSingleReportResponse.class))).thenReturn(new GetSingleReportResponse());

        // Act
        GetSingleReportResponse response = reportsService.getSingleReports(from, to, label, sensorType);

        // Assert
        assertNotNull(response);
        verify(sensorTypeLabelsValidator).validate(any(), any());
        verify(sensorsService).getSingleReports(any(), any(), any(), any());
        verify(modelMapper).map(any(), eq(GetSingleReportResponse.class));
    }

    private ReportData createMockReportData() {
        return ReportData.builder()
                .id(UUID.randomUUID())
                .from(System.currentTimeMillis())
                .to(System.currentTimeMillis() + 1000)
                .sensorsInstantEntries(Collections.emptyMap())
                .build();
    }

    private SensorData createMockSensorData() {
        return SensorData.builder()
                .from(System.currentTimeMillis())
                .to(System.currentTimeMillis() + 1000)
                .label("Label")
                .sensorType("SensorType")
                .entries(Collections.emptyMap())
                .build();
    }

    private static Report getReport() {
        var result = new Report();
        result.setId(UUID.randomUUID());
        return result;
    }
}

