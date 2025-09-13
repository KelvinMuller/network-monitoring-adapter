package com.networkmonitoring.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {
    
    private final MeterRegistry registry;
    private final Map<String, SynchronizedDouble> gauges = new ConcurrentHashMap<>();

    public void updateGauge(String metricName, Double value, Map<String, String> labels) {
        String metricId = createMetricId(metricName, labels);
        
        SynchronizedDouble gauge = gauges.computeIfAbsent(metricId, 
            id -> createGauge(metricName, value, labels));
        
        gauge.setValue(value);
        log.debug("Updated gauge '{}' to {}", metricName, value);
    }

    private String createMetricId(String metricName, Map<String, String> labels) {
        return metricName + labels.toString();
    }

    private SynchronizedDouble createGauge(String metricName, Double initialValue, Map<String, String> labels) {
        SynchronizedDouble holder = new SynchronizedDouble(initialValue);
        
        Gauge.Builder<SynchronizedDouble> gaugeBuilder = Gauge.builder(metricName, holder, SynchronizedDouble::getValue);
        
        // Add tags one by one
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            gaugeBuilder.tag(entry.getKey(), entry.getValue());
        }
        
        gaugeBuilder.register(registry);
        
        log.info("Created gauge: {} with labels: {}", metricName, labels);
        return holder;
    }

    private static class SynchronizedDouble {
        private double value;

        public SynchronizedDouble(double initialValue) {
            this.value = initialValue;
        }

        public synchronized void setValue(double value) {
            this.value = value;
        }

        public synchronized double getValue() {
            return value;
        }
    }
}