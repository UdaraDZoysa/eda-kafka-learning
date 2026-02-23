package com.harsha.common.events;

public record OrderPlacedEvent(
        String orderId,
        String product,
        int quantity,
        String currency
) {}
