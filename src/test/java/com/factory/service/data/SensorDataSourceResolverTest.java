package com.factory.service.data;

import com.factory.domain.SensorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class SensorDataSourceResolverTest {

    @Mock
    private SensorDataSource dataSource1;

    @Mock
    private SensorDataSource dataSource2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDataSource() {
        // Arrange
        List<SensorDataSource> dataSourceList = Arrays.asList(dataSource1, dataSource2);
        when(dataSource1.getSensorType()).thenReturn("Type1");
        when(dataSource2.getSensorType()).thenReturn("Type2");

        SensorDataSourceResolver resolver = new SensorDataSourceResolver(dataSourceList);

        // Act
        SensorDataSource result1 = resolver.getDataSource(SensorType.of("Type1"));
        SensorDataSource result2 = resolver.getDataSource(SensorType.of("Type2"));

        // Assert
        assertEquals(dataSource1, result1);
        assertEquals(dataSource2, result2);
    }
}
