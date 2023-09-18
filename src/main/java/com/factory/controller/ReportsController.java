package com.factory.controller;

import com.factory.openapi.api.ReportsApi;
import com.factory.openapi.model.CreateReportRequest;
import com.factory.openapi.model.CreateReportResponse;
import com.factory.openapi.model.GetReportDetailsResponse;
import com.factory.openapi.model.GetReportListResponse;
import com.factory.openapi.model.GetSingleReportResponse;
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
    public ResponseEntity<GetReportDetailsResponse> getReportDetails(final UUID id) {
        return ResponseEntity.ok(reportsService.getReportDetails(id));
    }

    @Override
    public ResponseEntity<GetReportListResponse> getReportsList(@NotNull @Valid final Long from,
                                                                @NotNull @Valid final Long to) {
        return ResponseEntity.ok(reportsService.getReportsList(from, to));
    }

    @Override
    public ResponseEntity<GetSingleReportResponse> getSingleReports(@NotNull @Valid final Long from,
                                                                    @NotNull @Valid final Long to,
                                                                    final String label,
                                                                    final String sensorType) {
        return ResponseEntity.ok(reportsService.getSingleReports(from, to, label, sensorType));
    }
}
