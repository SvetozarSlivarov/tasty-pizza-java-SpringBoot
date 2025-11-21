package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String username) {
        super("User with username '" + username + "' not found",
                ErrorCode.USER_NOT_FOUND,
                HttpStatus.NOT_FOUND);
    }
}
