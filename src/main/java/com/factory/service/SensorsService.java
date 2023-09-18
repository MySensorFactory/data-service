package com.factory.service;

import com.factory.domain.ReportData;
import com.factory.domain.SensorData;
import com.factory.domain.SensorLabel;
import com.factory.domain.SensorType;

import java.time.ZonedDateTime;
import java.util.Map;

public interface SensorsService {

    ReportData getSensorsData(ZonedDateTime from,
                              ZonedDateTime to,
                              Map<SensorType, SensorLabel> labels);

    SensorData getSingleReports(final Long from,
                                final Long to,
                                final SensorLabel label,
                                final SensorType sensorType);
}
