package com.factory.config;

import com.factory.config.dto.DataSource;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class DataSourceConfig {
    private Map<String, DataSource> dataSources;
}
