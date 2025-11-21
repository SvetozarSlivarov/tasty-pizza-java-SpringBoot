package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

public class IngredientNotFoundException extends BusinessException {

    public IngredientNotFoundException(Long id) {
        super("Ingredient with id: " + id + " not found",
                ErrorCode.INGREDIENT_NOT_FOUND,
                HttpStatus.NOT_FOUND);
    }
}
