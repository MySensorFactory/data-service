package com.factory.persistence.elasticsearch.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.Date;

@Document(indexName = "report")
@DynamicMapping(DynamicMappingValue.False)
@Getter
@Setter
@Setting(settingPath = "/elasticsearch/settings/lowercase_normalizer.json")
@Mapping(mappingPath = "/elasticsearch/mapping/report_mapping.json")
public class ReportDataEsModel {

    public static final String LOWER_CASE_NORMALIZER = "lower_case_normalizer";
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword, normalizer = LOWER_CASE_NORMALIZER)
    private String label;

    @Field(type = FieldType.Text, normalizer = LOWER_CASE_NORMALIZER)
    private String name;

    @Field(type = FieldType.Text, normalizer = LOWER_CASE_NORMALIZER)
    private String description;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Date from;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Date to;

    @Field(type = FieldType.Nested)
    private ReportSensorLabelEsModel reportSensorLabels;

}
