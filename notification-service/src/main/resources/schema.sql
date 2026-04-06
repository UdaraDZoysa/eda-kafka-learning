CREATE INDEX idx_notification_delivery_ready
    ON notification_deliveries (status, next_retry_at);

CREATE INDEX idx_inbox_retry
    ON inbox_events(processed, next_retry_at);