package bg.svetozar.tastypizza.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException ex,
            ServletWebRequest request
    ) {
        String path = request.getRequest().getRequestURI();

        if (ex.getStatus().is5xxServerError()) {
            log.error("Business exception at {}: {}", path, ex.getMessage(), ex);
        } else {
            log.warn("Business exception at {}: {}", path, ex.getMessage());
        }

        ApiError body = new ApiError(
                Instant.now(),
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                path,
                ex.getCode()
        );

        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(
            Exception ex,
            ServletWebRequest request
    ) {
        String path = request.getRequest().getRequestURI();

        log.error("Unexpected error at {}: {}", path, ex.getMessage(), ex);

        ApiError body = new ApiError(
                Instant.now(),
                500,
                "Internal Server Error",
                "Unexpected error occurred",
                path,
                ErrorCode.INTERNAL_ERROR
        );

        return ResponseEntity.internalServerError().body(body);
    }
}
