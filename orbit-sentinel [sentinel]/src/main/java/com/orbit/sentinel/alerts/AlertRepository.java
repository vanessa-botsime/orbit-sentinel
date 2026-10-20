package com.orbit.sentinel.alerts;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    boolean existsByRuleNameAndActorAndCreatedAtAfter(String ruleName, String actor, Instant after);

    List<Alert> findTop100ByOrderByCreatedAtDesc();
}
