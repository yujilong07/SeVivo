package com.meilong.messenger.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record StatsResponse(
        Long targetId,
        String url,
        Instant from,
        Instant to,
        long totalChecks,
        long successfulChecks,
        BigDecimal uptimePercentage
) {
}
