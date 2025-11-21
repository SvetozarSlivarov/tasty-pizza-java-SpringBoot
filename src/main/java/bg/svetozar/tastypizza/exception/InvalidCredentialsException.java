package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super("Invalid username or password",
                ErrorCode.BAD_CREDENTIALS,
                HttpStatus.UNAUTHORIZED);
    }
}
