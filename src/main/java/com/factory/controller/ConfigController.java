package com.factory.controller;

import com.factory.config.dto.DataConfig;
import com.factory.mapping.DataConfigMapper;
import com.factory.openapi.api.ConfigApi;
import com.factory.openapi.model.Config;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConfigController implements ConfigApi {

    private final DataConfigMapper dataConfigMapper;

    private final DataConfig dataConfig;

    @Override
    public ResponseEntity<Config> getConfiguration() {
        return ResponseEntity.ok(dataConfigMapper.dataConfigToConfig(dataConfig));
    }
}
