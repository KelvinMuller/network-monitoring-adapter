package com.networkmonitoring.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "sensor")
public class SensorConfiguration {
    private List<String> numericKeys =new ArrayList<>();
}

