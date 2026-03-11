package com.harsha.payment_service.application.events;

import com.harsha.common.events.DomainEvent;

public interface DomainEventPublisher {
    void publish(
            String aggregateId,
            DomainEvent event
    );
}
