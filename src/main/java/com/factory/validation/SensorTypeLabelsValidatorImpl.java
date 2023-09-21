package com.factory.validation;

import com.factory.config.DataSourceConfig;
import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;
import com.factory.exception.ClientErrorException;
import com.factory.openapi.model.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class SensorTypeLabelsValidatorImpl implements SensorTypeLabelsValidator {
    private final DataSourceConfig dataSourceConfig;

    @Override
    public void validate(final Map<SensorType, SensorLabel> input) {
        final AtomicBoolean isOK = new AtomicBoolean(true);
        var badSensorTypes = new HashSet<>();
        var badLabels = new HashSet<>();
        input.forEach((key, value) -> {
            if (!dataSourceConfig.getDataSources().containsKey(key.getType())) {
                badSensorTypes.add(key.getType());
                isOK.set(false);
            } else {
                if (!dataSourceConfig.getDataSources().get(key.getType())
                        .getAvailableLabels()
                        .contains(value.getLabel())) {
                    badLabels.add(value.getLabel());
                    isOK.set(false);
                }
            }
        });
        if (!isOK.get()) {
            throw new ClientErrorException(Error.CodeEnum.INVALID_INPUT.toString(),
                    String.format("Request contains not supported sensor types: %s and not supported labels %s",
                            badSensorTypes, badLabels));
        }
    }

    @Override
    public void validate(final SensorType sensorType, final SensorLabel label) {
        if (!dataSourceConfig.getDataSources().containsKey(sensorType.getType())) {
            throw new ClientErrorException(Error.CodeEnum.INVALID_INPUT.toString(),
                    String.format("Request contains not supported sensor type: %s", sensorType.getType()));
        }
        if (!dataSourceConfig.getDataSources()
                .get(sensorType.getType())
                .getAvailableLabels()
                .contains(label.getLabel())) {
            throw new ClientErrorException(Error.CodeEnum.INVALID_INPUT.toString(),
                    String.format("Request contains not supported label %s", label.getLabel()));
        }
    }
}
