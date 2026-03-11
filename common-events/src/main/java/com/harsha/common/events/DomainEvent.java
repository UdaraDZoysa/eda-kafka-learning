package com.harsha.common.events;

public interface DomainEvent {
    EventType type();

    int version();
}
