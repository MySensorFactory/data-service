package com.factory.service.data;

import com.factory.domain.BasicSensorDataEntry;
import com.factory.domain.SensorLabel;

import java.time.ZonedDateTime;
import java.util.List;

public interface SensorDataSource {

    List<BasicSensorDataEntry> findByLabelAndTimeWindow(SensorLabel label, ZonedDateTime from, ZonedDateTime to);

    String getSensorType();
}
