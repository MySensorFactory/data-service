package com.factory.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.factory.config.DataSourceConfig;
import com.factory.config.dto.DataSource;
import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;
import com.factory.exception.ClientErrorException;
import com.factory.openapi.model.Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SensorTypeLabelsValidatorImplTest {

    @Mock
    private DataSourceConfig dataSourceConfig;

    @InjectMocks
    private SensorTypeLabelsValidatorImpl validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validate_WithValidInput_ShouldNotThrowException() {
        Map<SensorType, SensorLabel> input = new HashMap<>();
        when(dataSourceConfig.getDataSources()).thenReturn(createValidDataSources());

        assertDoesNotThrow(() -> validator.validate(input));
    }

    @Test
    void validate_WithInvalidSensorType_ShouldThrowClientErrorException() {
        Map<SensorType, SensorLabel> input = createInvalidSensorTypeInput();

        ClientErrorException exception = assertThrows(ClientErrorException.class, () -> validator.validate(input));
        assertEquals(Error.CodeEnum.INVALID_INPUT.toString(), exception.getCode());
        assertTrue(exception.getMessage().contains("Request contains not supported sensor types"));
    }

    @Test
    void validate_WithInvalidLabel_ShouldThrowClientErrorException() {
        Map<SensorType, SensorLabel> input = createInvalidLabelInput();

        ClientErrorException exception = assertThrows(ClientErrorException.class, () -> validator.validate(input));
        assertEquals(Error.CodeEnum.INVALID_INPUT.toString(), exception.getCode());
    }

    private Map<String, DataSource> createValidDataSources() {
        return new HashMap<>();
    }

    private Map<SensorType, SensorLabel> createInvalidSensorTypeInput() {
        Map<SensorType, SensorLabel> input = new HashMap<>();
        input.put(SensorType.of("InvalidType"), SensorLabel.of("ValidLabel"));
        return input;
    }

    private Map<SensorType, SensorLabel> createInvalidLabelInput() {
        Map<SensorType, SensorLabel> input = new HashMap<>();
        input.put(SensorType.of("ValidType"), SensorLabel.of("InvalidLabel"));
        return input;
    }
}
