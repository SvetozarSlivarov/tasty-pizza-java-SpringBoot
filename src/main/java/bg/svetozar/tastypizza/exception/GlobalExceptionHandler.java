package bg.svetozar.tastypizza.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    private String resolvePath(ServletWebRequest request) {
        try {
            return request.getRequest().getRequestURI();
        } catch (Exception e) {
            return null;
        }
    }

    private ApiError buildApiError(
            HttpStatus status,
            String message,
            String path,
            String code,
            Map<String, String> validationErrors
    ) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                code,
                validationErrors
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException ex,
            ServletWebRequest request
    ) {
        String path = resolvePath(request);
        HttpStatus status = ex.getStatus();

        log.warn("Business error at {}: {} ({})", path, ex.getMessage(), ex.getCode());

        ApiError body = buildApiError(
                status,
                ex.getMessage(),
                path,
                ex.getCode(),
                null
        );

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            ServletWebRequest request
    ) {
        String path = resolvePath(request);

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fieldError -> validationErrors.put(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ));

        log.warn("Validation error at {}: {}", path, validationErrors);

        ApiError body = buildApiError(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                path,
                ErrorCode.BAD_REQUEST,
                validationErrors
        );

        return ResponseEntity.badRequest().body(body);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex,
            ServletWebRequest request
    ) {
        String path = resolvePath(request);

        Map<String, String> validationErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            validationErrors.put(field, violation.getMessage());
        });

        log.warn("Constraint violation at {}: {}", path, validationErrors);

        ApiError body = buildApiError(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                path,
                ErrorCode.BAD_REQUEST,
                validationErrors
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(
            HttpMessageNotReadableException ex,
            ServletWebRequest request
    ) {
        String path = resolvePath(request);

        log.warn("Invalid request body at {}: {}", path, ex.getMessage());

        ApiError body = buildApiError(
                HttpStatus.BAD_REQUEST,
                "Invalid request body",
                path,
                ErrorCode.BAD_REQUEST,
                null
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            ServletWebRequest request
    ) {
        String path = resolvePath(request);

        String msg = String.format(
                "Invalid value '%s' for parameter '%s'",
                ex.getValue(),
                ex.getName()
        );

        log.warn("Type mismatch at {}: {}", path, msg);

        ApiError body = buildApiError(
                HttpStatus.BAD_REQUEST,
                msg,
                path,
                ErrorCode.BAD_REQUEST,
                null
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(
            MissingServletRequestParameterException ex,
            ServletWebRequest request
    ) {
        String path = resolvePath(request);

        String msg = String.format(
                "Required request parameter '%s' is missing",
                ex.getParameterName()
        );

        log.warn("Missing request parameter at {}: {}", path, msg);

        ApiError body = buildApiError(
                HttpStatus.BAD_REQUEST,
                msg,
                path,
                ErrorCode.BAD_REQUEST,
                null
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(
            DataIntegrityViolationException ex,
            ServletWebRequest request
    ) {
        String path = resolvePath(request);

        log.error("Data integrity violation at {}: {}", path, ex.getMessage(), ex);

        ApiError body = buildApiError(
                HttpStatus.CONFLICT,
                "Data integrity violation",
                path,
                ErrorCode.BAD_REQUEST,
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex,
            ServletWebRequest request
    ) {
        String path = resolvePath(request);

        log.warn("Illegal argument at {}: {}", path, ex.getMessage());

        ApiError body = buildApiError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                path,
                ErrorCode.BAD_REQUEST,
                null
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(
            Exception ex,
            ServletWebRequest request
    ) {
        String path = resolvePath(request);

        log.error("Unexpected error at {}: {}", path, ex.getMessage(), ex);

        ApiError body = buildApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error occurred",
                path,
                ErrorCode.INTERNAL_ERROR,
                null
        );

        return ResponseEntity.internalServerError().body(body);
    }
}
