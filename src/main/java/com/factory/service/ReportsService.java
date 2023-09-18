package com.factory.service;

import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;
import com.factory.exception.ClientErrorException;
import com.factory.openapi.model.CreateReportRequest;
import com.factory.openapi.model.CreateReportResponse;
import com.factory.openapi.model.Error;
import com.factory.openapi.model.GetReportDetailsResponse;
import com.factory.openapi.model.GetReportListResponse;
import com.factory.openapi.model.GetSingleReportResponse;
import com.factory.openapi.model.TimeRange;
import com.factory.persistence.entity.Report;
import com.factory.persistence.repository.ReportsRepository;
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

    @Transactional
    public CreateReportResponse createReports(final CreateReportRequest createReportRequest) {
        var report = modelMapper.map(createReportRequest, Report.class);
        var result = reportsRepository.save(report);
        return modelMapper.map(result, CreateReportResponse.class);
    }

    @Transactional
    public GetReportDetailsResponse getReportDetails(final UUID reportId) {
        var report = reportsRepository.findById(reportId)
                .orElseThrow(() -> new ClientErrorException(Error.CodeEnum.NOT_FOUND.toString(),
                        "Report with id " + reportId + " not found"));
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
        return modelMapper.map(result, GetReportListResponse.class);
    }

    public GetSingleReportResponse getSingleReports(final Long from,
                                                    final Long to,
                                                    final String label,
                                                    final String sensorType) {
        var data = sensorsService.getSingleReports(from, to, SensorLabel.of(label), SensorType.of(sensorType));
        return modelMapper.map(data, GetSingleReportResponse.class);
    }
}
