package com.factory.domain;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class Event {

    private UUID id;

    private ZonedDateTime timestamp;

    private String title;

    private Boolean isAlert;
}
