package com.meilong.messenger.service;

import com.meilong.messenger.dto.StatsResponse;
import com.meilong.messenger.model.HealthCheck;
import com.meilong.messenger.model.Target;
import com.meilong.messenger.repository.HealthCheckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
public class StatsService {
    private final TargetService targetService;
    private final HealthCheckRepository healthCheckRepository;

    public StatsService(TargetService targetService, HealthCheckRepository healthCheckRepository) {
        this.targetService = targetService;
        this.healthCheckRepository = healthCheckRepository;
    }

    @Transactional(readOnly = true)
    public StatsResponse getStats(long targetId, Instant from, Instant to) {
        Target target = targetService.get(targetId);
        List<HealthCheck> checks = healthCheckRepository.findForTargetBetween(targetId, from, to);
        long successfulChecks = checks.stream().filter(HealthCheck::isSuccessful).count();
        BigDecimal uptime = checks.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(successfulChecks)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(checks.size()), 2, RoundingMode.HALF_UP);
        return new StatsResponse(target.getId(), target.getUrl(), from, to, checks.size(),
                successfulChecks, uptime);
    }
}
