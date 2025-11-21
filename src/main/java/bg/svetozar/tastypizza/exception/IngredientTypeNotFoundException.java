package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

public class IngredientTypeNotFoundException extends BusinessException {

    public IngredientTypeNotFoundException(Long id) {
        super("Ingredient type with id: " + id + " not found",
                ErrorCode.INGREDIENT_TYPE_NOT_FOUND,
                HttpStatus.NOT_FOUND);
    }

    public IngredientTypeNotFoundException(String name) {
        super("Ingredient type with name: " + name + " not found",
                ErrorCode.INGREDIENT_TYPE_NOT_FOUND,
                HttpStatus.NOT_FOUND);
    }
}
