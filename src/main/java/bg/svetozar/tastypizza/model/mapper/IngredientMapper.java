package bg.svetozar.tastypizza.model.mapper;

import bg.svetozar.tastypizza.model.dto.ingredient.IngredientDto;
import bg.svetozar.tastypizza.model.dto.ingredient.IngredientWithTypeDto;
import bg.svetozar.tastypizza.model.dto.ingredientType.IngredientTypeDto;
import bg.svetozar.tastypizza.model.entity.Ingredient;
import bg.svetozar.tastypizza.model.entity.IngredientType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IngredientMapper {

    public IngredientDto toResponse(Ingredient ingredient) {
        if (ingredient == null) return null;

        return toResponse(ingredient, ingredient.getName());
    }

    public IngredientDto toResponse(Ingredient ingredient, String name) {
        if (ingredient == null) return null;

        return new IngredientDto(
                ingredient.getId(),
                name,
                ingredient.isDeleted(),
                ingredient.getDeletedAt()
        );
    }

    public List<IngredientDto> toResponseList(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(this::toResponse)
                .toList();
    }

    public IngredientWithTypeDto toWithTypeResponse(Ingredient ingredient) {
        if (ingredient == null) return null;

        String typeName = ingredient.getType() != null ? ingredient.getType().getName() : null;
        return toWithTypeResponse(ingredient, ingredient.getName(), typeName);
    }

    public IngredientWithTypeDto toWithTypeResponse(Ingredient ingredient, String name, String typeName) {
        if (ingredient == null) return null;

        IngredientType type = ingredient.getType();
        IngredientTypeDto typeResponse = null;

        if (type != null) {
            typeResponse = new IngredientTypeDto(
                    type.getId(),
                    typeName
            );
        }

        return new IngredientWithTypeDto(
                ingredient.getId(),
                name,
                typeResponse,
                ingredient.isDeleted(),
                ingredient.getDeletedAt()
        );
    }

    public List<IngredientWithTypeDto> toWithTypeResponseList(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(this::toWithTypeResponse)
                .toList();
    }
}
