package com.factory.config;

import com.factory.config.dto.EsConfig;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.mapping.context.MappingContext;

import java.io.IOException;
import com.factory.exception.StartupException;

@Configuration
@RequiredArgsConstructor
@EnableElasticsearchRepositories(basePackages = "com.factory.persistence.elasticsearch.repository")
@ComponentScan(basePackages = {"com.factory.persistence.elasticsearch.model"})
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {


    private final EsConfig esConfig;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {

        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(esConfig.getAddress())
                .build();

        try (var s = RestClients.create(clientConfiguration)) {
            return s.rest();
        } catch (final IOException e) {
            throw new StartupException("Cannot connect to ES server with address: " + esConfig.getAddress());
        }
    }

    @Bean
    public MappingContext<SimpleElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> elasticsearchMappingContext() {
        return new SimpleElasticsearchMappingContext();
    }
}
