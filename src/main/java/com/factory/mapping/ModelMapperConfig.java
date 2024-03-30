package com.factory.mapping;

import com.factory.domain.SensorData;
import com.factory.domain.*;
import com.factory.openapi.model.*;
import com.factory.openapi.model.Filter;
import com.factory.persistence.data.entity.Report;
import com.factory.persistence.data.entity.ReportSensorLabel;
import com.factory.persistence.elasticsearch.model.ReportDataEsModel;
import com.factory.persistence.elasticsearch.model.ReportSensorLabelEsModel;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        mapper.addConverter(createInstantDataGetReportDetailsResponseConverter());
        mapper.addConverter(createReportDataEsModelReportConverter(mapper));
        mapper.addConverter(createFilterFilterConverter());
        mapper.addConverter(createReportDataEsModelReportPreviewConverter(mapper));
        return mapper;
    }

    private static Converter<Long, ZonedDateTime> createLongZonedDateTimeConverter() {
        return new AbstractConverter<>() {
            @Override
            public ZonedDateTime convert(final Long input) {
                if (Objects.nonNull(input)) {
                    Instant instant = Instant.ofEpochMilli(input);
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
                    return input.toInstant().toEpochMilli();
                }
                return null;
            }
        };
    }

    private static Converter<UpsertReportRequest, Report> createReportRequestReportConverter(final ModelMapper mapper) {
        return new AbstractConverter<>() {
            @Override
            public Report convert(final UpsertReportRequest input) {
                if (Objects.nonNull(input)) {
                    var result = new Report();
                    result.setFrom(mapper.map(input.getTimeRange().getFrom(), ZonedDateTime.class));
                    result.setTo(mapper.map(input.getTimeRange().getTo(), ZonedDateTime.class));
                    result.setLabel(input.getLabel());
                    result.setDescription(input.getDescription());
                    result.setName(input.getName());
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

    private static Converter<ReportDataEsModel, ReportPreview> createReportDataEsModelReportPreviewConverter(final ModelMapper modelMapper) {
        return new AbstractConverter<>() {
            @Override
            public ReportPreview convert(final ReportDataEsModel input) {
                if (Objects.nonNull(input)) {
                    return ReportPreview.builder()
                            .id(UUID.fromString(input.getId()))
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

    private static Converter<UpsertReportRequest,ReportDataEsModel> createReportDataEsModelReportConverter(final ModelMapper modelMapper) {
        return new AbstractConverter<>() {
            @Override
            public ReportDataEsModel convert(final UpsertReportRequest input) {
                if (Objects.nonNull(input)) {
                    return ReportDataEsModel.builder()
                            .name(input.getName())
                            .description(input.getDescription())
                            .from(modelMapper.map(input.getTimeRange().getFrom(),ZonedDateTime.class))
                            .to(modelMapper.map(input.getTimeRange().getTo(), ZonedDateTime.class))
                            .label(input.getLabel())
                            .reportSensorLabels(input.getSensorLabels().entrySet().stream()
                                    .map(sl -> ReportSensorLabelEsModel.builder()
                                            .sensorType(sl.getKey())
                                            .label(sl.getValue())
                                            .build())
                                    .toList()
                            )
                            .build();
                }
                return null;
            }
        };
    }

    private static Converter<Filter, com.factory.domain.Filter> createFilterFilterConverter() {
        return new AbstractConverter<>() {
            @Override
            public com.factory.domain.Filter convert(final Filter input) {
                if (Objects.nonNull(input)) {
                    return com.factory.domain.Filter.builder()
                            .textFields(input.getTextFields())
                            .keywords(input.getKeywords())
                            .textQuery(input.getTextQuery())
                            .from(input.getFrom())
                            .to(input.getTo())
                            .build();
                }
                return null;
            }
        };
    }
}
