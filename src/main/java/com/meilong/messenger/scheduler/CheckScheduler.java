package com.meilong.messenger.scheduler;

import com.meilong.messenger.model.HealthCheck;
import com.meilong.messenger.model.Target;
import com.meilong.messenger.repository.HealthCheckRepository;
import com.meilong.messenger.repository.TargetRepository;
import com.meilong.messenger.service.CheckService;
import com.meilong.messenger.service.CheckService.CheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CheckScheduler {
    private static final Logger logger = LoggerFactory.getLogger(CheckScheduler.class);

    private final TargetRepository targetRepository;
    private final HealthCheckRepository healthCheckRepository;
    private final CheckService checkService;

    public CheckScheduler(
            TargetRepository targetRepository,
            HealthCheckRepository healthCheckRepository,
            CheckService checkService
    ) {
        this.targetRepository = targetRepository;
        this.healthCheckRepository = healthCheckRepository;
        this.checkService = checkService;
    }

    @Scheduled(fixedDelayString = "${monitor.check.interval-ms:60000}")
    public void checkTargets() {
        for (Target target : targetRepository.findAll()) {
            CheckResult result = checkService.checkUrl(target.getUrl());
            healthCheckRepository.save(new HealthCheck(
                    target,
                    Instant.now(),
                    result.statusCode(),
                    result.responseTimeMs(),
                    result.successful(),
                    result.errorMessage()
            ));
            if (result.successful()) {
                logger.info("Checked {}: HTTP {} in {} ms",
                        target.getUrl(), result.statusCode(), result.responseTimeMs());
            } else {
                logger.warn("Check failed for {}: {}", target.getUrl(), result.errorMessage());
            }
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
        }
    }
}
