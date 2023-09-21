package com.factory.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
@NoArgsConstructor
public class EventKey {
    private String key;
}
