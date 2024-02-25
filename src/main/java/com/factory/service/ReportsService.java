package com.factory.service;

import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;
import com.factory.exception.ClientErrorException;
import com.factory.mapping.CollectionMapper;
import com.factory.openapi.model.Error;
import com.factory.openapi.model.*;
import com.factory.persistence.data.entity.Report;
import com.factory.persistence.data.repository.ReportsRepository;
import com.factory.persistence.elasticsearch.model.ReportDataEsModel;
import com.factory.persistence.elasticsearch.repository.ReportsEsRepository;
import com.factory.validation.SensorTypeLabelsValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final ReportsRepository reportsRepository;
    private final SensorsService sensorsService;
    private final ModelMapper modelMapper;
    private final CollectionMapper collectionMapper;
    private final SensorTypeLabelsValidator sensorTypeLabelsValidator;
    private final ReportsEsRepository reportsEsRepository;

    @Transactional
    public UpsertReportResponse createReports(final UpsertReportRequest request) {
        validateUpsertReportRequest(request);
        var report = modelMapper.map(request, Report.class);
        var result = reportsRepository.save(report);
        saveReportToEsRepository(result.getId(), request);
        return modelMapper.map(result, UpsertReportResponse.class);
    }

    @Transactional
    public GetReportDetailsResponse getReportDetails(final UUID reportId) {
        var report = getReport(reportId);
        var instantData = sensorsService.getSensorsData(
                report.getFrom(),
                report.getTo(),
                report.getReportSensorLabels().stream()
                        .collect(Collectors.toMap(
                                lbl -> SensorType.of(lbl.getSensorType()),
                                lbl -> SensorLabel.of(lbl.getLabel())
                        )));
        var result = modelMapper.map(instantData, GetReportDetailsResponse.class);
        result.setId(reportId);
        result.setTimeRange(TimeRange.builder()
                .from(report.getFrom().toEpochSecond())
                .to(report.getTo().toEpochSecond())
                .build());
        return result;
    }

    public GetReportListResponse getReportsList(final Long from, final Long to) {
        var result = reportsRepository.findAllByTimeWindow(
                modelMapper.map(from, ZonedDateTime.class),
                modelMapper.map(to, ZonedDateTime.class)
        );
        return collectionMapper.reportListToDto(result);
    }

    public GetSingleReportResponse getSingleReports(final Long from,
                                                    final Long to,
                                                    final String label,
                                                    final String sensorType) {
        sensorTypeLabelsValidator.validate(SensorType.of(sensorType), SensorLabel.of(label));
        var data = sensorsService.getSingleReports(from, to, SensorLabel.of(label), SensorType.of(sensorType));
        return modelMapper.map(data, GetSingleReportResponse.class);
    }

    @Transactional
    public void deleteReport(final UUID id) {
        reportsRepository.deleteById(id);
        reportsEsRepository.deleteById(id.toString());
    }

    public GetReportListResponse searchForReports(final SearchReportsRequest request) {
        return null;
    }

    @Transactional
    public UpsertReportResponse updateReport(final UUID id, final UpsertReportRequest request) {
        validateUpsertReportRequest(request);
        var newReport = modelMapper.map(request, Report.class);
        var oldReport = getReport(id);
        oldReport.update(newReport, () -> reportsRepository.saveAndFlush(oldReport));
        var result = saveReportToEsRepository(id, request);
        return UpsertReportResponse.builder()
                .id(UUID.fromString(result.getId()))
                .build();
    }

    private ReportDataEsModel saveReportToEsRepository(UUID id, UpsertReportRequest request) {
        var esModel = modelMapper.map(request, ReportDataEsModel.class);
        esModel.setId(id.toString());
        return reportsEsRepository.save(esModel);
    }

    private Report getReport(UUID reportId) {
        return reportsRepository.findById(reportId)
                .orElseThrow(() -> new ClientErrorException(Error.CodeEnum.NOT_FOUND.toString(),
                        "Report with id " + reportId + " not found"));
    }

    private void validateUpsertReportRequest(UpsertReportRequest request) {
        sensorTypeLabelsValidator.validate(collectionMapper.stringMapToSensorTypeLabelMap(request.getSensorLabels()));
    }
}
