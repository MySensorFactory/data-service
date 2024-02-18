package com.factory.controller;

import com.factory.openapi.api.ReportsApi;
import com.factory.openapi.model.*;
import com.factory.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReportsController implements ReportsApi {

    private final ReportsService reportsService;

    @Override
    public ResponseEntity<CreateReportResponse> createReport(@Valid final CreateReportRequest createReportRequest) {
        return ResponseEntity.ok(reportsService.createReports(createReportRequest));
    }

    @Override
    public ResponseEntity<Void> deleteReport(final UUID id) {
        return null;
    }

    @Override
    public ResponseEntity<GetReportDetailsResponse> getReportDetails(final UUID id) {
        return ResponseEntity.ok(reportsService.getReportDetails(id));
    }

    @Override
    public ResponseEntity<GetSingleReportResponse> getSingleReports(@NotNull @Valid final Long from,
                                                                    @NotNull @Valid final Long to,
                                                                    final String label,
                                                                    final String sensorType) {
        return ResponseEntity.ok(reportsService.getSingleReports(from, to, label, sensorType));
    }

    @Override
    public ResponseEntity<GetReportListResponse> searchReports(@Valid final SearchReportsRequest searchReportsRequest) {
        return null;
    }

    @Override
    public ResponseEntity<CreateReportRequest> updateReport(final UUID id) {
        return null;
    }
}
