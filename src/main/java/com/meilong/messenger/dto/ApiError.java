package com.meilong.messenger.dto;

import java.time.Instant;

public record ApiError(Instant timestamp, int status, String message) {
}
