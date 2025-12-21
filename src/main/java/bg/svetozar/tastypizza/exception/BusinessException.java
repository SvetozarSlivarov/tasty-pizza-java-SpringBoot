package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public abstract class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;
    private final Map<String, Object> details;

    protected BusinessException(String message, String code, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = null;
    }

    protected BusinessException(String message, String code, HttpStatus status, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details;
    }

    protected BusinessException(String message, String code, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.status = status;
        this.details = null;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
    public Map<String, Object> getDetails() { return details; }
}
