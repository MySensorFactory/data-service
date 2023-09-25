package com.factory.service.data;

import com.factory.config.DataSourceConfig;
import com.factory.domain.EventKey;
import com.factory.domain.SensorData;
import com.factory.domain.SensorDataEntry;
import com.factory.domain.SensorLabel;
import com.factory.persistence.data.repository.CompressorStateRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompressorStateDataSource implements SensorDataSource {

    private final DataSourceConfig dataSourceConfig;
    private final CompressorStateRepository compressorStateRepository;
    private final ModelMapper modelMapper;

    @Override
    public SensorData findByLabelAndTimeWindow(final SensorLabel label,
                                               final ZonedDateTime from,
                                               final ZonedDateTime to) {
        return SensorData.builder()
                .from(modelMapper.map(from, Long.class))
                .to(modelMapper.map(to, Long.class))
                .sensorType(getSensorType())
                .label(label.getLabel())
                .entries(compressorStateRepository.findByTimeWindowAndLabel(label.getLabel(), from, to)
                        .stream()
                        .map(sd -> SensorDataEntry.builder()
                                .eventKey(sd.getAuditData().getEventKey())
                                .label(sd.getAuditData().getLabel())
                                .sensorType(getSensorType())
                                .timestamp(modelMapper.map(sd.getAuditData().getTimestamp(), Long.class))
                                .data(Map.of(
                                        "noiseLevel", sd.getNoiseLevel(),
                                        "vibrationAmplitude", sd.getVibrationAmplitude(),
                                        "vibrationFrequency", sd.getVibrationFrequency()
                                ))
                                .build())
                        .collect(Collectors.toMap(sd -> EventKey.of(sd.getEventKey()), Function.identity())))
                .build();
    }

    @Override
    public String getSensorType() {
        return dataSourceConfig.getDataSources().get("compressorState").getSensorType();
    }
}
