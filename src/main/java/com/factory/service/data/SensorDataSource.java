package com.factory.service.data;

import com.factory.domain.SensorData;
import com.factory.domain.SensorLabel;

import java.time.ZonedDateTime;

public interface SensorDataSource {

    SensorData findByLabelAndTimeWindow(SensorLabel label, ZonedDateTime from, ZonedDateTime to);

    String getSensorType();
}
