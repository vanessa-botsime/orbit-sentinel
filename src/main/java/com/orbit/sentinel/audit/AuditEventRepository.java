package com.orbit.sentinel.audit;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    boolean existsByEventId(String eventId);

    Optional<AuditEvent> findTopByOrderByIdDesc();

    Page<AuditEvent> findByType(String type, Pageable pageable);

    Page<AuditEvent> findByActor(String actor, Pageable pageable);

    Page<AuditEvent> findByTypeAndActor(String type, String actor, Pageable pageable);

    List<AuditEvent> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);

    long countByTypeAndActorAndOccurredAtGreaterThanEqual(String type, String actor, Instant from);
}
