package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

public class ApiErrors {

    private ApiErrors() {}

    public static ApiError fromBusiness(
            BusinessException ex,
            String path,
            String traceId
    ) {
        HttpStatus status = ex.getStatus();
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                path,
                ex.getCode(),
                traceId,
                ex.getDetails(),
                null
        );
    }

    public static ApiError validation(
            HttpStatus status,
            String message,
            String path,
            String code,
            String traceId,
            Map<String, String> validationErrors
    ) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                code,
                traceId,
                null,
                validationErrors
        );
    }

    public static ApiError internal(String path, String traceId) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                "Unexpected error occurred.",
                path,
                ErrorCode.INTERNAL_ERROR,
                traceId,
                null,
                null
        );
    }
}
