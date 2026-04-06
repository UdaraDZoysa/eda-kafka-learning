package com.harsha.common.events;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope(
        UUID eventId,
        String aggregateId,
        EventType eventType,
        int eventVersion,
        Instant occurredAt,
        JsonNode payload,
        String source
) {}
