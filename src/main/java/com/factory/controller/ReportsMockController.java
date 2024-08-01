package com.factory.controller;

import com.factory.openapi.api.ReportsApi;
import com.factory.openapi.model.*;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Profile("!mock")
@RestController
public class ReportsMockController implements ReportsApi {
    @Override
    public ResponseEntity<UpsertReportResponse> createReport(@Valid UpsertReportRequest upsertReportRequest) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteReport(UUID id) {
        return null;
    }

    @Override
    public ResponseEntity<GetReportDetailsResponse> getReportDetails(UUID id) {
        return null;
    }

    @Override
    public ResponseEntity<GetSingleReportResponse> getSingleReports(@NotNull @Valid Long from, @NotNull @Valid Long to, String label, String sensorType) {
        return null;
    }

    @Override
    public ResponseEntity<GetReportListResponse> searchReports(@Valid SearchReportsRequest searchReportsRequest) {
        return null;
    }

    @Override
    public ResponseEntity<UpsertReportResponse> updateReport(UUID id, @Valid UpsertReportRequest upsertReportRequest) {
        return null;
    }
}
