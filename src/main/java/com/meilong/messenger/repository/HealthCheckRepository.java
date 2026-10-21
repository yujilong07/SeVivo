package com.meilong.messenger.repository;

import com.meilong.messenger.model.HealthCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long> {
    @Query("""
            select check from HealthCheck check
            where check.target.id = :targetId
              and check.checkedAt >= :from
              and check.checkedAt <= :to
            order by check.checkedAt
            """)
    List<HealthCheck> findForTargetBetween(
            @Param("targetId") Long targetId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
