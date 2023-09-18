package com.factory.service.data;

import com.factory.domain.EventKey;
import com.factory.domain.SensorData;
import com.factory.domain.SensorDataEntry;
import com.factory.domain.SensorLabel;
import com.factory.persistence.repository.MeanFlowRateRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowRateMeanDataSource implements SensorDataSource {

    @Value("${data.sources.flowRateMean}")
    private String sensorType;

    private final MeanFlowRateRepository meanFlowRateRepository;
    private final ModelMapper modelMapper;

    @Override
    public SensorData findByLabelAndTimeWindow(final SensorLabel label,
                                               final ZonedDateTime from,
                                               final ZonedDateTime to) {
        return SensorData.builder()
                .from(modelMapper.map(from, Long.class))
                .to(modelMapper.map(to, Long.class))
                .sensorType(sensorType)
                .label(label.getLabel())
                .entries(meanFlowRateRepository.findByTimeWindowAndLabel(label.getLabel(), from, to)
                        .stream()
                        .map(sd -> SensorDataEntry.builder()
                                .eventKey(sd.getAuditData().getEventKey())
                                .label(sd.getAuditData().getLabel())
                                .timestamp(modelMapper.map(sd.getAuditData().getTimestamp(), Long.class))
                                .data(Map.of(
                                        "flowRateMean", sd.getValue()
                                ))
                                .build())
                        .collect(Collectors.toMap(sd -> EventKey.of(sd.getEventKey()), Function.identity())))
                .build();
    }

    @Override
    public String getSensorType() {
        return sensorType;
    }
}
