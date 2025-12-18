package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

public class IngredientTypeInUseException extends BusinessException {

    public IngredientTypeInUseException(Long typeId) {
        super(
                "Cannot delete ingredient type. It is used by existing ingredients.",
                ErrorCode.TYPE_IN_USE,
                HttpStatus.CONFLICT
        );
    }
}
