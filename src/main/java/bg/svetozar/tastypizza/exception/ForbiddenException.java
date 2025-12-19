package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(
                message,
                ErrorCode.FORBIDDEN,
                HttpStatus.FORBIDDEN
        );
    }
}
