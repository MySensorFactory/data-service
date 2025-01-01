package com.factory.config;

import com.factory.config.dto.EsConfig;
import com.factory.exception.StartupException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableElasticsearchRepositories(basePackages = "com.factory.persistence.elasticsearch.repository")
@ComponentScan(basePackages = {"com.factory.persistence.elasticsearch.model"})
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    private final EsConfig esConfig;

    @Override
    public @NotNull ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(esConfig.getAddress())
                .build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        try {
            RestClient httpClient = RestClient.builder(
                    HttpHost.create(esConfig.getAddress())
            ).build();

            RestClientTransport transport = new RestClientTransport(
                    httpClient,
                    new JacksonJsonpMapper()
            );

            return new ElasticsearchClient(transport);
        } catch (Exception e) {
            throw new StartupException("Cannot connect to ES server with address: " + esConfig.getAddress());
        }
    }
}