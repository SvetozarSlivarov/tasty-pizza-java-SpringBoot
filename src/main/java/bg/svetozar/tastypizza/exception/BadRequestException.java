package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class BadRequestException extends BusinessException {

    public BadRequestException(String message, String code) {
        super(message, code, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, String code, Map<String, Object> details) {
        super(message, code, HttpStatus.BAD_REQUEST, details);
    }
}
