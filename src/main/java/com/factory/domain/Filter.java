package com.factory.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class Filter {
    private Map<String, String> keywords;
    private List<String> textFields;
    private String textQuery;
    private Long from;
    private Long to;
}
