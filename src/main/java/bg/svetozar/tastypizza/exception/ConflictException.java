package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ConflictException extends BusinessException {

    public ConflictException(String message, String code) {
        super(message, code, HttpStatus.CONFLICT);
    }

    public ConflictException(String message, String code, Map<String, Object> details) {
        super(message, code, HttpStatus.CONFLICT, details);
    }
}
