package com.factory.config.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@ConfigurationProperties("data")
public class DataSourceConfig {
    private Map<String, DataSource> dataSources;

    @Data
    @NoArgsConstructor
    public static class DataSource {
        private String sensorType;
        private String displayName;
        private List<Label> availableLabels;
    }

    @Data
    @NoArgsConstructor
    public static class Label {
        private String label;
        private String displayName;
    }
}
