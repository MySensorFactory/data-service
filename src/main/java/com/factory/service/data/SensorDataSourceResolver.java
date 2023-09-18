package com.factory.service.data;

import com.factory.domain.SensorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SensorDataSourceResolver {

    private final Map<String, SensorDataSource> sources = new HashMap<>();

    @Autowired
    public SensorDataSourceResolver(final List<SensorDataSource> sources) {
        sources.forEach(source -> this.sources.put(source.getSensorType(),source));
    }

    public SensorDataSource getDataSource(final SensorType sensorType) {
        return sources.get(sensorType.getType());
    }

}
