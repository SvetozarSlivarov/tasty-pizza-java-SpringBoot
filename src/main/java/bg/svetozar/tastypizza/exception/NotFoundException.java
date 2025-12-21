package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message, String code) {
        super(message, code, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String message, String code, Map<String, Object> details) {
        super(message, code, HttpStatus.NOT_FOUND, details);
    }

    public NotFoundException(String message) {
        super(message, ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
