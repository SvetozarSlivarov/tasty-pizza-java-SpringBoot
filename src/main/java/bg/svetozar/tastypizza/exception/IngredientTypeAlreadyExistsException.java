package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

public class IngredientTypeAlreadyExistsException extends BusinessException {

    public IngredientTypeAlreadyExistsException(String name) {
        super("Ingredient type with name: " + name + " already exists",
                ErrorCode.INGREDIENT_TYPE_ALREADY_EXISTS,
                HttpStatus.CONFLICT);
    }
}
