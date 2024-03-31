package com.factory.config.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EsConfig {
    private String indexName;
    private String address;
}
