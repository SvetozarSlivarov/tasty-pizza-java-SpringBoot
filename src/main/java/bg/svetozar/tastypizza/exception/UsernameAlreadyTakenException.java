package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

public class UsernameAlreadyTakenException extends BusinessException {

    public UsernameAlreadyTakenException(String username) {
        super("Username '" + username + "' is already taken",
                ErrorCode.USERNAME_ALREADY_TAKEN,
                HttpStatus.CONFLICT);
    }
}