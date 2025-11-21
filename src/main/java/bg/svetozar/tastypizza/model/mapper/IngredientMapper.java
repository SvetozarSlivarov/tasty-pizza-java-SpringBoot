package bg.svetozar.tastypizza.model.mapper;

import bg.svetozar.tastypizza.model.dto.ingredient.IngredientResponse;
import bg.svetozar.tastypizza.model.dto.ingredient.IngredientWithTypeResponse;
import bg.svetozar.tastypizza.model.dto.ingredientType.IngredientTypeResponse;
import bg.svetozar.tastypizza.model.entity.Ingredient;
import bg.svetozar.tastypizza.model.entity.IngredientType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IngredientMapper {

    public IngredientResponse toResponse(Ingredient ingredient) {
        if (ingredient == null) return null;

        return new IngredientResponse(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.isDeleted(),
                ingredient.getDeletedAt()
        );
    }

    public List<IngredientResponse> toResponseList(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(this::toResponse)
                .toList();
    }

    public IngredientWithTypeResponse toWithTypeResponse(Ingredient ingredient) {
        if (ingredient == null) return null;

        IngredientType type = ingredient.getType();
        IngredientTypeResponse typeResponse = null;

        if (type != null) {
            typeResponse = new IngredientTypeResponse(
                    type.getId(),
                    type.getName()
            );
        }

        return new IngredientWithTypeResponse(
                ingredient.getId(),
                ingredient.getName(),
                typeResponse,
                ingredient.isDeleted(),
                ingredient.getDeletedAt()
        );
    }

    public List<IngredientWithTypeResponse> toWithTypeResponseList(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(this::toWithTypeResponse)
                .toList();
    }
}
