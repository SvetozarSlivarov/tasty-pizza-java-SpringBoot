package bg.svetozar.tastypizza.model.mapper;

import bg.svetozar.tastypizza.model.dto.ingredientType.IngredientTypeResponse;
import bg.svetozar.tastypizza.model.entity.IngredientType;
import org.springframework.stereotype.Component;

@Component
public class IngredientTypeMapper {

    public IngredientTypeResponse toResponse(IngredientType entity) {
        return new IngredientTypeResponse(
                entity.getId(),
                entity.getName()
        );
    }
}