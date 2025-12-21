package bg.svetozar.tastypizza.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
import java.util.List;
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
            Map<String, Object> details,
            Map<String, String> validationErrors
    ) {
        String traceId = MDC.get("traceId");
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                code,
                traceId,
                details,
                validationErrors
        );
    }

    private String leafKey(String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) return propertyPath;
        int dot = propertyPath.lastIndexOf('.');
        return dot >= 0 ? propertyPath.substring(dot + 1) : propertyPath;
    }

    private Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur;
    }

    private String jsonFieldFromJacksonPath(List<JsonMappingException.Reference> path) {
        if (path == null || path.isEmpty()) return null;
        JsonMappingException.Reference last = path.get(path.size() - 1);
        if (last.getFieldName() != null) return last.getFieldName();
        if (last.getIndex() >= 0) return String.valueOf(last.getIndex());
        return null;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException ex, ServletWebRequest request) {
        String path = resolvePath(request);
        HttpStatus status = ex.getStatus();

        log.warn("Business error status={} code={} path={} traceId={} message={}",
                status.value(), ex.getCode(), path, MDC.get("traceId"), ex.getMessage());

        Map<String, Object> details = null;
        try {
            details = ex.getDetails();
        } catch (Exception ignored) {
        }

        ApiError body = buildApiError(status, ex.getMessage(), path, ex.getCode(), details, null);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, ServletWebRequest request) {
        String path = resolvePath(request);

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                validationErrors.put(fe.getField(), fe.getDefaultMessage())
        );

        log.warn("Validation error path={} traceId={} errors={}", path, MDC.get("traceId"), validationErrors);

        ApiError body = buildApiError(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                path,
                ErrorCode.BAD_REQUEST,
                null,
                validationErrors
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, ServletWebRequest request) {
        String path = resolvePath(request);

        Map<String, String> validationErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String key = leafKey(v.getPropertyPath() == null ? null : v.getPropertyPath().toString());
            validationErrors.put(key, v.getMessage());
        });

        log.warn("Constraint violation path={} traceId={} errors={}", path, MDC.get("traceId"), validationErrors);

        ApiError body = buildApiError(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                path,
                ErrorCode.BAD_REQUEST,
                null,
                validationErrors
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, ServletWebRequest request) {
        String path = resolvePath(request);
        Throwable root = rootCause(ex);

        if (root instanceof InvalidFormatException ife) {
            String field = jsonFieldFromJacksonPath(ife.getPath());

            Map<String, String> validationErrors = new HashMap<>();
            if (field != null) {
                // ако е enum, изкарваме allowed values
                Class<?> target = ife.getTargetType();
                if (target != null && target.isEnum()) {
                    Object[] constants = target.getEnumConstants();
                    String allowed = constants == null ? "" : java.util.Arrays.toString(constants);
                    validationErrors.put(field, "Invalid value. Allowed: " + allowed);
                } else {
                    validationErrors.put(field, "Invalid value.");
                }
            }

            log.warn("Invalid JSON format path={} traceId={} field={} target={} msg={}",
                    path, MDC.get("traceId"), field, ife.getTargetType(), ife.getOriginalMessage());

            ApiError body = buildApiError(
                    HttpStatus.BAD_REQUEST,
                    "Invalid request body",
                    path,
                    ErrorCode.INVALID_ENUM_VALUE,
                    null,
                    validationErrors.isEmpty() ? null : validationErrors
            );

            return ResponseEntity.badRequest().body(body);
        }

        log.warn("Invalid request body path={} traceId={} msg={}", path, MDC.get("traceId"), ex.getMessage());

        ApiError body = buildApiError(
                HttpStatus.BAD_REQUEST,
                "Invalid request body",
                path,
                ErrorCode.BAD_REQUEST,
                null,
                null
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, ServletWebRequest request) {
        String path = resolvePath(request);

        String msg = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());

        Map<String, String> validationErrors = new HashMap<>();
        validationErrors.put(ex.getName(), "Invalid value");

        log.warn("Type mismatch path={} traceId={} msg={}", path, MDC.get("traceId"), msg);

        ApiError body = buildApiError(
                HttpStatus.BAD_REQUEST,
                msg,
                path,
                ErrorCode.BAD_REQUEST,
                null,
                validationErrors
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex, ServletWebRequest request) {
        String path = resolvePath(request);

        String msg = String.format("Required request parameter '%s' is missing", ex.getParameterName());

        Map<String, String> validationErrors = new HashMap<>();
        validationErrors.put(ex.getParameterName(), "Required");

        log.warn("Missing param path={} traceId={} msg={}", path, MDC.get("traceId"), msg);

        ApiError body = buildApiError(
                HttpStatus.BAD_REQUEST,
                msg,
                path,
                ErrorCode.BAD_REQUEST,
                null,
                validationErrors
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, ServletWebRequest request) {
        String path = resolvePath(request);

        log.warn("Illegal argument path={} traceId={} msg={}", path, MDC.get("traceId"), ex.getMessage());

        ApiError body = buildApiError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                path,
                ErrorCode.BAD_REQUEST,
                null,
                null
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, ServletWebRequest request) {
        String path = resolvePath(request);

        log.error("Data integrity violation path={} traceId={}", path, MDC.get("traceId"), ex);

        ApiError body = buildApiError(
                HttpStatus.CONFLICT,
                "Data integrity violation",
                path,
                ErrorCode.CONFLICT,
                null,
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        String path = request.getRequestURI();

        ApiError body = buildApiError(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                path,
                ErrorCode.UNAUTHORIZED,
                null,
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, ServletWebRequest request) {
        String path = resolvePath(request);

        log.error("Unexpected error path={} traceId={}", path, MDC.get("traceId"), ex);

        ApiError body = buildApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error occurred",
                path,
                ErrorCode.INTERNAL_ERROR,
                null,
                null
        );

        return ResponseEntity.internalServerError().body(body);
    }
}
