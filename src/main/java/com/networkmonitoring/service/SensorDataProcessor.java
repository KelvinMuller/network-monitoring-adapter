package com.networkmonitoring.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networkmonitoring.configuration.SensorConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorDataProcessor {
    
    private final ObjectMapper objectMapper;
    private final MetricsService metricsService;
    private final SensorConfiguration sensorConfiguration;
    

    public void processMessage(String message) throws Exception {
        JsonNode node = objectMapper.readTree(message);
        
        Map<String, String> labels = new HashMap<>();
        Map<String, Double> numericValues = new HashMap<>();
        
        splitFieldsByType(node, labels, numericValues);
        updateMetrics(numericValues, labels);
        
        log.info("Processed {} numeric metrics with {} labels", 
                numericValues.size(), labels.size());
    }

    private void splitFieldsByType(JsonNode node, Map<String, String> labels, Map<String, Double> numericValues) {
        node.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            
            if (sensorConfiguration.getNumericKeys().contains(key)) {
                processNumericField(entry, numericValues);
            } else {
                processTextField(entry, labels);
            }
        });
    }

    private void processNumericField(Map.Entry<String, JsonNode> entry, Map<String, Double> numericValues) {
        try {
            double value = entry.getValue().asDouble();
            numericValues.put(entry.getKey(), value);
            log.trace("Numeric field: {} = {}", entry.getKey(), value);
        } catch (Exception e) {
            log.warn("Failed to parse numeric value for key '{}': {}", entry.getKey(), e.getMessage());
        }
    }

    private void processTextField(Map.Entry<String, JsonNode> entry, Map<String, String> labels) {
        String value = entry.getValue().asText();
        labels.put(entry.getKey(), value);
        log.trace("Text field (label): {} = {}", entry.getKey(), value);
    }

    private void updateMetrics(Map<String, Double> numericValues, Map<String, String> labels) {
        numericValues.forEach((metricName, value) -> 
            metricsService.updateGauge(metricName, value, labels));
    }
}
