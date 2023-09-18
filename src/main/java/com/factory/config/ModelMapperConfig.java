package com.factory.config;

import com.factory.domain.EventKey;
import com.factory.domain.ReportData;
import com.factory.domain.SensorData;
import com.factory.domain.SensorDataEntry;
import com.factory.domain.SensorType;
import com.factory.openapi.model.CreateReportRequest;
import com.factory.openapi.model.GetReportDetailsResponse;
import com.factory.openapi.model.GetReportListResponse;
import com.factory.openapi.model.GetSingleReportResponse;
import com.factory.openapi.model.InstantData;
import com.factory.openapi.model.ReportPreview;
import com.factory.openapi.model.TimeRange;
import com.factory.persistence.entity.Report;
import com.factory.persistence.entity.ReportSensorLabel;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        var mapper = new ModelMapper();
        mapper.addConverter(createLongZonedDateTimeConverter());
        mapper.addConverter(createZonedDateTimeLongConverter());
        mapper.addConverter(createReportRequestReportConverter(mapper));
        mapper.addConverter(createReportReportPreviewConverter(mapper));
        mapper.addConverter(createSensorDataGetSingleReportResponseConverter());
        mapper.addConverter(createListGetReportListResponseConverter(mapper));
        mapper.addConverter(createInstantDataGetReportDetailsResponseConverter());
        return mapper;
    }

    private static Converter<Long, ZonedDateTime> createLongZonedDateTimeConverter() {
        return new AbstractConverter<>() {
            @Override
            public ZonedDateTime convert(final Long input) {
                if (Objects.nonNull(input)) {
                    Instant instant = Instant.ofEpochSecond(input);
                    return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
                }
                return null;
            }
        };
    }

    private static Converter<ZonedDateTime, Long> createZonedDateTimeLongConverter() {
        return new AbstractConverter<>() {
            @Override
            public Long convert(final ZonedDateTime input) {
                if (Objects.nonNull(input)) {
                    return input.toEpochSecond();
                }
                return null;
            }
        };
    }

    private static Converter<CreateReportRequest, Report> createReportRequestReportConverter(final ModelMapper mapper) {
        return new AbstractConverter<>() {
            @Override
            public Report convert(final CreateReportRequest input) {
                if (Objects.nonNull(input)) {
                    var result = new Report();
                    result.setFrom(mapper.map(input.getTimeRange().getFrom(), ZonedDateTime.class));
                    result.setTo(mapper.map(input.getTimeRange().getTo(), ZonedDateTime.class));
                    result.setLabel(input.getLabel());
                    result.setReportSensorLabels(input.getSensorLabels().entrySet().stream()
                            .map(entry -> {
                                var sensorLabel = new ReportSensorLabel();
                                sensorLabel.setReport(result);
                                sensorLabel.setSensorType(entry.getKey());
                                sensorLabel.setLabel(entry.getValue());
                                return sensorLabel;
                            })
                            .collect(Collectors.toSet()));
                    return result;
                }
                return null;
            }
        };
    }

    private static Converter<Report, ReportPreview> createReportReportPreviewConverter(final ModelMapper modelMapper) {
        return new AbstractConverter<>() {
            @Override
            public ReportPreview convert(final Report input) {
                if (Objects.nonNull(input)) {
                    return ReportPreview.builder()
                            .id(input.getId())
                            .label(input.getLabel())
                            .timeRange(TimeRange.builder()
                                    .from(modelMapper.map(input.getFrom(), Long.class))
                                    .to(modelMapper.map(input.getTo(), Long.class))
                                    .build())
                            .build();
                }
                return null;
            }
        };
    }

    private static Converter<List<Report>, GetReportListResponse> createListGetReportListResponseConverter(final ModelMapper modelMapper) {
        return new AbstractConverter<>() {
            @Override
            public GetReportListResponse convert(final List<Report> input) {
                if (Objects.nonNull(input)) {
                    return GetReportListResponse.builder()
                            .results(input.stream()
                                    .map(r -> modelMapper.map(r, ReportPreview.class))
                                    .toList())
                            .build();
                }
                return null;
            }
        };
    }

    private static Converter<ReportData, GetReportDetailsResponse> createInstantDataGetReportDetailsResponseConverter() {
        return new AbstractConverter<>() {
            @Override
            public GetReportDetailsResponse convert(final ReportData input) {
                if (Objects.nonNull(input)) {
                    return GetReportDetailsResponse.builder()
                            .id(input.getId())
                            .timeRange(TimeRange.builder()
                                    .from(input.getFrom())
                                    .to(input.getTo())
                                    .build())
                            .dataBySensorType(input.getSensorsInstantEntries().entrySet().stream()
                                    .collect(Collectors.toMap(entry -> entry.getKey().getType(),
                                            this::toInstantData)))
                            .build();
                }
                return null;
            }

            private InstantData toInstantData(final Map.Entry<SensorType, Map<EventKey, SensorDataEntry>> sensorTypeEntry) {
                return InstantData.builder()
                        .dataByEventKey(sensorTypeEntry.getValue().entrySet().stream()
                                .collect(Collectors.toMap(eventKeyEntry -> eventKeyEntry.getKey().getKey(),
                                        this::toSensorData)))
                        .build();
            }

            private com.factory.openapi.model.SensorData toSensorData(final Map.Entry<EventKey, SensorDataEntry> eventKeyEntry) {
                return com.factory.openapi.model.SensorData.builder()
                        .values(eventKeyEntry.getValue().getData().entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> BigDecimal.valueOf(entry.getValue())))
                        )
                        .sensorType(eventKeyEntry.getValue().getSensorType())
                        .eventKey(UUID.fromString(eventKeyEntry.getKey().getKey()))
                        .sensorType(eventKeyEntry.getValue().getSensorType())
                        .timestamp(eventKeyEntry.getValue().getTimestamp())
                        .label(eventKeyEntry.getValue().getLabel())
                        .build();
            }
        };
    }


    private static Converter<SensorData, GetSingleReportResponse> createSensorDataGetSingleReportResponseConverter() {
        return new AbstractConverter<>() {
            @Override
            public GetSingleReportResponse convert(final SensorData input) {
                if (Objects.nonNull(input)) {
                    return GetSingleReportResponse.builder()
                            .results(input.getEntries().entrySet().stream()
                                    .map(sde -> com.factory.openapi.model.SensorData.builder()
                                            .eventKey(UUID.fromString(sde.getKey().getKey()))
                                            .label(sde.getValue().getLabel())
                                            .timestamp(sde.getValue().getTimestamp())
                                            .sensorType(sde.getValue().getSensorType())
                                            .values(sde.getValue().getData().entrySet().stream()
                                                    .collect(Collectors.toMap(
                                                            Map.Entry::getKey,
                                                            entry -> BigDecimal.valueOf(entry.getValue()))))
                                            .build())
                                    .toList())
                            .build();
                }
                return null;
            }
        };
    }
}
