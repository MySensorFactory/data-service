package com.factory.service;

import com.factory.domain.Filter;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

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
    //TODO: refactor it !!!
    public GetReportDetailsResponse getReportDetails(final UUID reportId) {
        var report = getReport(reportId);
        var instantData = sensorsService.getSensorsData(
                report.getFrom(),
                report.getTo(),
                collectionMapper.decomposeReportSensorLabelToMap(report.getReportSensorLabels()));
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

    //TODO: refactor
    public GetReportListResponse searchForReports(final SearchReportsRequest request) {
        var filter = modelMapper.map(request.getFilter(), Filter.class);
        Pageable pageable = PageRequest.of(request.getPage(), request.getPageSize(), getSorting(request));
        var result = reportsEsRepository.search(pageable, filter).stream().map(SearchHit::getContent).toList();
        return GetReportListResponse.builder()
                .results(result.stream().map(r -> modelMapper.map(r, ReportPreview.class)).toList())
                .build();
    }

    private static Sort getSorting(SearchReportsRequest request) {
        if (Objects.nonNull(request.getSorting())) {
            return Sort.by(request.getSorting().stream().map(s -> {
                        if (s.getOrder().equals(Sorting.OrderEnum.ASC)) {
                            return Sort.Order.asc(s.getName());
                        }
                        return Sort.Order.desc(s.getName());
                    }
            ).toList());
        }

        return Sort.unsorted();
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
