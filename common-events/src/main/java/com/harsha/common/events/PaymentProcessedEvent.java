package com.harsha.common.events;

public record PaymentProcessedEvent(
        String orderId,
        String userId,
        boolean success
) implements DomainEvent {
    @Override
    public EventType type() {

        return EventType.PAYMENT_PROCESSED;
    }

    @Override
    public int version() {

        return 1;
    }
}
