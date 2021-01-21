package com.uber.uberapi.services.messagequeue;

// Different types of message
// A driver updates their location - location tracking service
// booking is requested, booking service gives message for driverMatchingService
// schedulingService

// An adaptor
// We can plugin multiple message queues / backends here
public interface MessageQueue {
    void sendMessage(String topic, MQMessage message);
    MQMessage consumeMessage(String topic);
}
