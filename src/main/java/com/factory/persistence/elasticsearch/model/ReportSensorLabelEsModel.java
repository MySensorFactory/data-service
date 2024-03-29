package com.factory.persistence.elasticsearch.model;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import static com.factory.persistence.elasticsearch.model.ReportDataEsModel.LOWER_CASE_NORMALIZER;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSensorLabelEsModel {
    private String id;

    @Field(type = FieldType.Keyword, normalizer = LOWER_CASE_NORMALIZER)
    private String label;

    @Field(type = FieldType.Keyword, normalizer = LOWER_CASE_NORMALIZER)
    private String sensorType;
}
