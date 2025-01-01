package com.factory.controller;

import com.factory.openapi.api.ReportsApi;
import com.factory.openapi.model.*;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
//@CrossOrigin
public class ReportsMockController implements ReportsApi {
    private final Map<UUID, GetReportDetailsResponse> reports = new HashMap<>();


    public ReportsMockController() {
        initializePreexistingReports();
    }

    private void initializePreexistingReports() {
        List<Map<String, String>> preexistingReports = getPreexistingReports();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");

        preexistingReports.forEach(report -> {
            UUID id = UUID.randomUUID();
            String[] dateRange = report.get("dateRange").split(" - ");
            LocalDateTime fromDate = LocalDateTime.parse(dateRange[0], formatter);
            LocalDateTime toDate = LocalDateTime.parse(dateRange[1], formatter);
            TimeRange timeRange = new TimeRange()
                    .from(fromDate.toInstant(ZoneOffset.UTC).toEpochMilli())
                    .to(toDate.toInstant(ZoneOffset.UTC).toEpochMilli());
            GetReportDetailsResponse reportDetails = new GetReportDetailsResponse()
                    .id(id)
                    .name(report.get("name"))
                    .description(report.get("description"))
                    .timeRange(timeRange)
                    .label(report.get("label"))
                    .includedSensors(Arrays.stream(report.get("includedSensors").split(",")).toList())
                    .dataBySensorType(createSampleDataBySensorType(report.get("includedSensors")));
            reports.put(id, reportDetails);
        });
    }

    private List<Map<String, String>> getPreexistingReports() {
        List<Map<String, String>> preexistingReports = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Map<String, String> report = new HashMap<>();
            report.put("name", "Very important report from compressor");
            report.put("dateRange", "7/2/2024 8:00 - 8/3/2024 8:00");
            report.put("description", "Lorem ipsum abba alfa beta gamma delta epsilon gamma " +
                    "salutation epsilon gamma epsilon gamma epsilon gamma epsilon gamma epsilon" +
                    "lorem ipsum abba alfa gamma delta epsilon gamma epsilon gamma");
            report.put("label", "beforeCompressor");
            report.put("includedSensors", "temperature,pressure,flowRate,composition,compressorState");
            preexistingReports.add(report);
        }
        return preexistingReports;
    }

    private Map<String, List<SensorData>> createSampleDataBySensorType(String includedSensors) {
        Map<String, List<SensorData>> dataBySensorType = new HashMap<>();
        String[] sensorTypes = includedSensors.split(",");
        for (String sensorType : sensorTypes) {
            dataBySensorType.put(sensorType, generateData(sensorType, 1));
        }
        return dataBySensorType;
    }


    @Override
    public ResponseEntity<UpsertReportResponse> createReport(@Valid UpsertReportRequest upsertReportRequest) {
        UUID newId = UUID.randomUUID();
        GetReportDetailsResponse newReport = createReportFromRequest(newId, upsertReportRequest);
        reports.put(newId, newReport);
        return ResponseEntity.ok(new UpsertReportResponse().id(newId));
    }

    @Override
    public ResponseEntity<Void> deleteReport(UUID id) {
        reports.remove(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<GetReportDetailsResponse> getReportDetails(UUID id) {
        GetReportDetailsResponse report = reports.get(id);
        if (report != null) {
            return ResponseEntity.ok(report);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @SneakyThrows
    public ResponseEntity<GetReportListResponse> searchReports(@Valid SearchReportsRequest searchReportsRequest) {
        Thread.sleep(700);
        List<ReportPreview> previews = new ArrayList<>();
        reports.forEach((key, report) -> {
            ReportPreview preview = new ReportPreview()
                    .id(key)
                    .includedSensors(new HashSet<>(report.getDataBySensorType().keySet()).stream().toList())
                    .label(report.getLabel())
                    .name(report.getName())
                    .timeRange(report.getTimeRange());
            previews.add(preview);
        });
        GetReportListResponse response = new GetReportListResponse()
                .results(searchReportsRequest.getPageSize() < previews.size() ? previews.subList(0, searchReportsRequest.getPageSize()) : previews)
                .totalItems(100);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UpsertReportResponse> updateReport(String id, @Valid UpsertReportRequest upsertReportRequest) {
        if (reports.containsKey(UUID.fromString(id))) {
            GetReportDetailsResponse updatedReport = createReportFromRequest(UUID.fromString(id), upsertReportRequest);
            reports.put(UUID.fromString(id), updatedReport);
            return ResponseEntity.ok(new UpsertReportResponse().id(UUID.fromString(id)));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private GetReportDetailsResponse createReportFromRequest(UUID id, UpsertReportRequest request) {
        return new GetReportDetailsResponse()
                .id(id)
                .name(request.getName())
                .label(request.getLabel())
                .includedSensors(request.getIncludedSensors())
                .description(request.getDescription())
                .timeRange(request.getTimeRange())
                .dataBySensorType(reports.get(id).getDataBySensorType());
    }

    private List<SensorData> generateData(String sensorType, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(days);
        List<SensorData> data = new ArrayList<>();
        Random random = new Random();

        double min, max;
        switch (sensorType) {
            case "temperature":
                min = 5;
                max = 40;
                break;
            case "pressure":
                min = 2;
                max = 8;
                break;
            case "flowRate":
                min = 0;
                max = 300;
                break;
            case "composition":
                min = 0;
                max = 1;
                break;
            case "compressorState":
                min = 0;
                max = 1;
                break;
            default:
                min = 0;
                max = 100;
        }

        while (startDate.isBefore(now) || startDate.isEqual(now)) {
            Map<String, BigDecimal> values = HomeMockController.generateValues(sensorType, random, max, min);

            data.add(new SensorData()
                    .timestamp(startDate.toInstant(ZoneOffset.UTC).toEpochMilli())
                    .values(values)
            );
            startDate = startDate.plusMinutes(15);
        }

        return data;
    }
}