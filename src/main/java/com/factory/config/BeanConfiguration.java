package com.factory.config;

import com.factory.config.dto.EsConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    @ConfigurationProperties("data")
    public DataSourceConfig dataSourceConfig() {
        return new DataSourceConfig();
    }

    @Bean
    @ConfigurationProperties("es")
    public EsConfig esConfig() {
        return new EsConfig();
    }
}
