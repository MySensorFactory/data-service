package com.factory.config.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties("es")
public class EsConfig {
    private String indexName;
    private String address;
}
