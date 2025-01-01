package com.factory.service;

import com.factory.domain.BasicSensorDataEntry;
import com.factory.domain.SensorType;
import com.factory.exception.ClientErrorException;
import com.factory.mapping.CommonMapper;
import com.factory.mapping.ReportsMapper;
import com.factory.openapi.model.Error;
import com.factory.openapi.model.*;
import com.factory.persistence.data.entity.Report;
import com.factory.persistence.data.repository.ReportsRepository;
import com.factory.persistence.elasticsearch.model.ReportDataEsModel;
import com.factory.persistence.elasticsearch.repository.ReportsEsRepository;
import com.factory.validation.SensorTypeLabelsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final ReportsRepository reportsRepository;

    private final SensorsService sensorsService;

    private final ReportsMapper reportsMapper;

    private final SensorTypeLabelsValidator sensorTypeLabelsValidator;

    private final ReportsEsRepository reportsEsRepository;

    @Transactional
    public UpsertReportResponse createReports(final UpsertReportRequest request) {
        validateUpsertReportRequest(request);
        var report = reportsMapper.map(request);
        var result = reportsRepository.save(report);
        saveReportToEsRepository(result.getId(), request);
        return reportsMapper.mapToResponse(result);
    }

    @Transactional
    public GetReportDetailsResponse getReportDetails(final UUID reportId) {
        var report = getReport(reportId);
        Map<SensorType, List<BasicSensorDataEntry>> reportData =
                sensorsService.getSensorsData(
                        report.getFrom(),
                        report.getTo(),
                        reportsMapper.mapToSensorLabel(report.getLabel()),
                        reportsMapper.mapSensorTypes(report.getIncludedSensors())
                );
        return reportsMapper.map(report, reportData);
    }

    @Transactional
    public void deleteReport(final UUID id) {
        reportsRepository.deleteById(id);
        reportsEsRepository.deleteById(id.toString());
    }

    public GetReportListResponse searchForReports(final SearchReportsRequest request) {
        var result = reportsEsRepository.search(
                        reportsMapper.mapToPageable(request),
                        reportsMapper.map(request.getFilter()))
                .stream()
                .map(SearchHit::getContent)
                .toList();
        return reportsMapper.mapToResponseFromEsModel(result);
    }

    @Transactional
    public UpsertReportResponse updateReport(final UUID id, final UpsertReportRequest request) {
        validateUpsertReportRequest(request);
        var newReport = reportsMapper.map(request);
        var oldReport = getReport(id);
        oldReport.update(newReport, () -> reportsRepository.saveAndFlush(oldReport));
        var result = saveReportToEsRepository(id, request);
        return reportsMapper.mapToResponseFromEsModel(result);
    }

    private ReportDataEsModel saveReportToEsRepository(final UUID id, final UpsertReportRequest request) {
        var esModel = reportsMapper.mapEsModel(id, request);
        return reportsEsRepository.save(esModel);
    }

    private Report getReport(final UUID reportId) {
        return reportsRepository.findById(reportId)
                .orElseThrow(() -> new ClientErrorException(Error.CodeEnum.NOT_FOUND.toString(),
                        "Report with id " + reportId + " not found"));
    }

    private void validateUpsertReportRequest(final UpsertReportRequest request) {
//        sensorTypeLabelsValidator.validate(collectionMapper.stringMapToSensorTypeLabelMap(request.ge()));
    }
}
