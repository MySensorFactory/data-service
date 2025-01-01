package com.factory;

import com.factory.config.dto.DataConfig;
import com.factory.config.dto.DataSourceConfig;
import com.factory.config.dto.EsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@EnableConfigurationProperties({
        DataConfig.class,
        DataSourceConfig.class,
        EsConfig.class
})
public class DataServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataServiceApplication.class, args);
    }

}
