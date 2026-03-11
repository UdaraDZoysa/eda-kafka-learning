package com.harsha.common.events;

public record OrderPlacedEvent(
        String orderId,
        String product,
        int quantity
) implements DomainEvent {
    @Override
    public int version() {
        return 1;
    }

    @Override
    public EventType type() {
        return EventType.ORDER_PLACED;
    }
}
