package com.harsha.order_service.api.dto;

public record PlaceOrderRequest(
        String product,
        String userId,
        int quantity
) {}
