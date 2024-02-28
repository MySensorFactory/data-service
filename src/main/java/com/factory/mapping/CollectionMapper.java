package com.factory.mapping;

import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;
import com.factory.openapi.model.GetReportListResponse;
import com.factory.openapi.model.ReportPreview;
import com.factory.persistence.data.entity.Report;
import com.factory.persistence.data.entity.ReportSensorLabel;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionMapper {

    private final ModelMapper modelMapper;

    public GetReportListResponse reportListToDto(final List<Report> input) {
        if (Objects.nonNull(input)) {
            return GetReportListResponse.builder()
                    .results(input.stream()
                            .map(r -> modelMapper.map(r, ReportPreview.class))
                            .toList())
                    .build();
        }
        return null;
    }

    public Map<SensorType, SensorLabel> stringMapToSensorTypeLabelMap(final Map<String, String> input) {
        if (Objects.nonNull(input)) {
            return input.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> SensorType.of(entry.getKey()),
                            entry -> SensorLabel.of(entry.getValue())));
        }
        return Map.of();
    }

    public Map<SensorType, SensorLabel> decomposeReportSensorLabelToMap(final Set<ReportSensorLabel> reportSensorLabels) {
        return reportSensorLabels.stream()
                .collect(Collectors.toMap(
                        lbl -> SensorType.of(lbl.getSensorType()),
                        lbl -> SensorLabel.of(lbl.getLabel())
                ));
    }
}
