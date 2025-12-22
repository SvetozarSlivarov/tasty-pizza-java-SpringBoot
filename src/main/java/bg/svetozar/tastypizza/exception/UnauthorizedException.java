package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(message, ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(String message, Map<String, Object> details) {
        super(message, ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, details);
    }

    public UnauthorizedException(String message, String code) {
        super(message, code, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(String message, String code, Map<String, Object> details) {
        super(message, code, HttpStatus.UNAUTHORIZED, details);
    }
}
