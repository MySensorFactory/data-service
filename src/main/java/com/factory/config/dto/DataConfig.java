package com.factory.config.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties("config")
public class DataConfig {
    private List<TimeRangeOption> timeRangeOptions;
    private List<LabeledValue> sortOptions;
    private Map<String, Map<String, String>> unitMapping;
    private List<String> wideSensors;

    @Data
    public static class LabeledValue {
        private String label;
        private String value;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class TimeRangeOption extends LabeledValue {
        private int daysCount;
    }
}


