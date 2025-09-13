package com.networkmonitoring.consumer;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.networkmonitoring.service.SensorDataProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorConsumer {
 
 private final SensorDataProcessor sensorDataProcessor;

 @KafkaListener(topics = "sensor-data", groupId = "sensor-group")
 public void consumeMessage(String message) {
     try {
         log.debug("Received sensor message: {}", message);
         sensorDataProcessor.processMessage(message);
         log.debug("Successfully processed sensor message");
     } catch (Exception e) {
         log.error("Failed to process sensor message: {}", message, e);
         // Consider adding dead letter queue or retry logic here
     }
 }
}
