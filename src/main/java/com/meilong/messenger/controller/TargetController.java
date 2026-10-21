package com.meilong.messenger.controller;

import com.meilong.messenger.dto.StatsResponse;
import com.meilong.messenger.dto.TargetRequest;
import com.meilong.messenger.dto.TargetResponse;
import com.meilong.messenger.model.Target;
import com.meilong.messenger.service.StatsService;
import com.meilong.messenger.service.TargetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/targets")
public class TargetController {
    private final TargetService targetService;
    private final StatsService statsService;

    public TargetController(TargetService targetService, StatsService statsService) {
        this.targetService = targetService;
        this.statsService = statsService;
    }

    @PostMapping
    public ResponseEntity<TargetResponse> create(@RequestBody TargetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(targetService.create(request)));
    }

    @GetMapping
    public List<TargetResponse> findAll() {
        return targetService.findAll().stream().map(TargetController::toResponse).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        targetService.delete(id);
    }

    @GetMapping("/{id}/stats")
    public StatsResponse getStats(
            @PathVariable long id,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        Instant end = to == null ? Instant.now() : to;
        Instant start = from == null ? end.minus(24, ChronoUnit.HOURS) : from;
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("'from' must be before or equal to 'to'");
        }
        return statsService.getStats(id, start, end);
    }

    private static TargetResponse toResponse(Target target) {
        return new TargetResponse(target.getId(), target.getUrl());
    }
}
