package com.factory.config.dto;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties("config")
public class DataConfig {
    List<LabeledValue> availableSensors;
    List<LabeledValue> availableLabels;
    List<LabeledValue> sortOptions;
    List<TimeRangeOption> timeRangeOptions;
    Map<String, Map<String, String>> unitMapping;
    List<String> wideSensors;

    @Data
    public static class LabeledValue {
        private String label;
        private String value;
    }

    @Data
    public static class TimeRangeOption {
        private String label;
        private String value;
        private int daysCount;
    }
}


