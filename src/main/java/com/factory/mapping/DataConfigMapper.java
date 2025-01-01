package com.factory.mapping;

import com.factory.config.dto.DataConfig;
import com.factory.openapi.model.Config;
import com.factory.openapi.model.ConfigTimeRangeOptionsInner;
import com.factory.openapi.model.LabeledValue;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface DataConfigMapper {

    Config dataConfigToConfig(DataConfig dataConfig);

    LabeledValue map(DataConfig.LabeledValue labeledValue);

    ConfigTimeRangeOptionsInner map(DataConfig.TimeRangeOption timeRangeOption);
}