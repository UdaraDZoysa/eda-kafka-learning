package com.harsha.common.events;

public record PaymentProcessedEvent(
        String orderId,
        boolean success
) {}
