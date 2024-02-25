package com.factory.controller;

import com.factory.openapi.api.ReportsApi;
import com.factory.openapi.model.*;
import com.factory.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReportsController implements ReportsApi {

    private final ReportsService reportsService;

    @Override
    public ResponseEntity<UpsertReportResponse> createReport(@Valid final UpsertReportRequest request) {
        return ResponseEntity.created(URI.create("")).body(reportsService.createReports(request));
    }

    @Override
    public ResponseEntity<Void> deleteReport(final UUID id) {
        reportsService.deleteReport(id);
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<GetReportListResponse> searchReports(@Valid SearchReportsRequest request) {
        return ResponseEntity.ok(reportsService.searchForReports(request));
    }

    @Override
    public ResponseEntity<UpsertReportResponse> updateReport(final UUID id, final UpsertReportRequest request) {
        return ResponseEntity.ok(reportsService.updateReport(id,request));
    }
}
