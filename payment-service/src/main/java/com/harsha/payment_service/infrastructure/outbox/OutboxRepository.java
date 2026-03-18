package com.harsha.payment_service.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, String> {
    @Modifying
    @Query("""
        DELETE FROM OutboxEvent e
            WHERE e.published = true 
                AND e.createdAt < :cutoff
        """
    )
    void deletePublishedOlderThan(@Param("cutoff") Instant cutoff);

    @Modifying
    @Query(
            value = """
                SELECT *
                    FROM outbox_events
                    WHERE published = false
                    ORDER BY created_at 
                    LIMIT 20
                    FOR UPDATE SKIP LOCKED
                """,
            nativeQuery = true
    )
    List<OutboxEvent> lockNextBatch();
}
