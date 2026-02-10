package com.harsha.order_service.events;

public record OrderPlacedEvent (
        String orderId,
        String product,
        int quantity
){}


