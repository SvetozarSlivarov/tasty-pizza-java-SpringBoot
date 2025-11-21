package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

public class DrinkNotFoundException extends BusinessException {

    public DrinkNotFoundException(Long id) {
        super("Drink with id: " + id + " not found",
                ErrorCode.DRINK_NOT_FOUND,
                HttpStatus.NOT_FOUND);
    }
}
