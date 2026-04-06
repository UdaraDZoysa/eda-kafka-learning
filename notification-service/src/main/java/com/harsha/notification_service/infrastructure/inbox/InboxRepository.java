package com.harsha.notification_service.infrastructure.inbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface InboxRepository extends JpaRepository<InboxEvent, UUID> {
    @Query(value = """
         SELECT *
            FROM inbox_events
            WHERE processed = false
                AND next_retry_at <= CURRENT_TIMESTAMP
            ORDER BY next_retry_at
            LIMIT 20
            FOR UPDATE SKIP LOCKED
        """,
            nativeQuery = true
    )
    List<InboxEvent> lockNextBatch();
}
