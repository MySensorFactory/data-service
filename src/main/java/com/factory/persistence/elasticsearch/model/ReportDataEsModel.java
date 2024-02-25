package com.factory.persistence.elasticsearch.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.ZonedDateTime;
import java.util.List;

@Document(indexName = "report")
@DynamicMapping(DynamicMappingValue.False)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Setting(settingPath = "/elasticsearch/settings/lowercase_normalizer_analyzer.json")
@Mapping(mappingPath = "/elasticsearch/mapping/report_mapping.json")
@Builder
public class ReportDataEsModel {

    public static final String LOWER_CASE_NORMALIZER = "lower_case_normalizer";
    public static final String LOWER_CASE_ANALYZER = "lower_case_analyzer";
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword, normalizer = LOWER_CASE_NORMALIZER)
    private String label;

    @Field(type = FieldType.Text, analyzer = LOWER_CASE_ANALYZER)
    private String name;

    @Field(type = FieldType.Text, analyzer = LOWER_CASE_ANALYZER)
    private String description;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private ZonedDateTime from;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private ZonedDateTime to;

    @Field(type = FieldType.Nested)
    private List<ReportSensorLabelEsModel> reportSensorLabels;

}
