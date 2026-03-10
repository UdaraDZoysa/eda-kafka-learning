package com.harsha.order_service.application.events;

import com.harsha.common.events.EventType;

public interface DomainEventPublisher {
    void publish(
            String aggregateId,
            EventType eventType,
            Object event
    );
}
