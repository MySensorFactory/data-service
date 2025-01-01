package com.factory.mapping;

import com.factory.domain.BasicSensorDataEntry;
import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;
import com.factory.openapi.model.*;
import com.factory.persistence.data.entity.Report;
import com.factory.persistence.data.entity.ReportSensor;
import com.factory.persistence.elasticsearch.model.ReportDataEsModel;
import com.factory.persistence.elasticsearch.model.ReportSensorLabelEsModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.factory.openapi.model.Sorting.OrderEnum.ASC;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        uses = {CommonMapper.class},
        imports = {Map.class}
)
public interface ReportsMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "from", source = "timeRange.from")
    @Mapping(target = "to", source = "timeRange.to")
    @Mapping(target = "includedSensors", expression = "java(map(report,request.getIncludedSensors()))")
    Report map(UpsertReportRequest request);

    @Mapping(target = "sensorType", source = "sensorType")
    @Mapping(target = "report", source = "report")
    @Mapping(target = "id", ignore = true)
    ReportSensor map(Report report, String sensorType);

    default Set<ReportSensor> map(final Report report, final List<String> includedSensors) {
        return includedSensors.stream()
                .map(s -> map(report, s))
                .collect(Collectors.toSet());
    }

    @Mapping(target = "timeRange", expression = "java(commonMapper.map(reportDataEsModel.getFrom(),reportDataEsModel.getTo()))")
    @Mapping(target = "includedSensors", expression = "java(reportDataEsModel.getReportSensorLabels().stream().map(ReportSensorLabelEsModel::getSensorType).toList())")
    ReportPreview mapEsModel(ReportDataEsModel reportDataEsModel);

    @Mapping(target = "timeRange", expression = "java(commonMapper.map(report.getFrom(),report.getTo()))")
    @Mapping(target = "label", source = "report.label")
    @Mapping(target = "includedSensors", expression = "java(dataBySensorType.keySet().stream().map(SensorType::getType).toList())")
    @Mapping(target = "dataBySensorType", expression = "java(mapDataBySensorType(dataBySensorType))")
    GetReportDetailsResponse map(Report report, Map<SensorType, List<BasicSensorDataEntry>> dataBySensorType);

    default Map<String, List<SensorData>> mapDataBySensorType(Map<SensorType, List<BasicSensorDataEntry>> value) {
        return value.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getType(),
                        entry -> entry.getValue().stream()
                                .map(data -> SensorData.builder()
                                        .timestamp(data.getTimestamp())
                                        .values(data.getData().entrySet().stream().collect(
                                                Collectors.toMap(Map.Entry::getKey,
                                                        e -> BigDecimal.valueOf(e.getValue()))
                                        ))
                                        .build()
                                )
                                .collect(Collectors.toList())
                ));
    }

    @Mapping(target = "id", expression = "java(id.toString())")
    @Mapping(target = "from", source = "request.timeRange.from")
    @Mapping(target = "to", source = "request.timeRange.to")
    @Mapping(target = "reportSensorLabels", source = "request.includedSensors")
    ReportDataEsModel mapEsModel(UUID id, UpsertReportRequest request);

    @Mapping(target = "sensorType", expression = "java(includedSensor)")
    @Mapping(target = "label", ignore = true)
    @Mapping(target = "id", ignore = true)
    ReportSensorLabelEsModel map(String includedSensor);

    com.factory.domain.Filter map(Filter filter);

    default GetReportListResponse mapToResponseFromEsModel(final List<ReportDataEsModel> reports){
        return GetReportListResponse.builder()
                .results(reports.stream().map(this::mapEsModel).toList())
                .totalItems(reports.size())
                .build();
    }

    Map<SensorType, SensorLabel> map(Map<String, String> input);

    @Mapping(target = "label", expression = "java(sensorLabel)")
    SensorLabel mapToSensorLabel(String sensorLabel);

    @Mapping(target = "type", source = "sensorType")
    SensorType map(ReportSensor reportSensor);

    Set<SensorType> mapSensorTypes(Set<ReportSensor> reportSensors);

    UpsertReportResponse mapToResponse(Report report);

    UpsertReportResponse mapToResponseFromEsModel(ReportDataEsModel report);

    default Sort map(final SearchReportsRequest request) {
        if (request.getSorting() != null) {
            return Sort.by(request.getSorting().stream().map(s -> {
                        if (s.getOrder().equals(ASC)) {
                            return Sort.Order.asc(s.getName());
                        }
                        return Sort.Order.desc(s.getName());
                    }
            ).toList());
        }

        return Sort.unsorted();
    }

    default Pageable mapToPageable(SearchReportsRequest request) {
        return PageRequest.of(
                request.getPage(),
                request.getPageSize(),
                map(request)
        );
    }
}
