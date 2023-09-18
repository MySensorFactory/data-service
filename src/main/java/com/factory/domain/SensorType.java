package com.factory.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
public class SensorType {
    private String type;
}
