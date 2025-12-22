package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message, String code) {
        super(message, code, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String message, String code, Map<String, Object> details) {
        super(message, code, HttpStatus.FORBIDDEN, details);
    }

    public ForbiddenException(String message) {
        super(message, ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN);
    }
}
