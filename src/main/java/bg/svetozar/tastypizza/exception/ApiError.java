package bg.svetozar.tastypizza.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String code,
        Map<String, String> validationErrors
) {}