package bg.svetozar.tastypizza.model.mapper;

import bg.svetozar.tastypizza.model.dto.ingredientType.IngredientTypeDto;
import bg.svetozar.tastypizza.model.entity.IngredientType;
import org.springframework.stereotype.Component;

@Component
public class IngredientTypeMapper {

    public IngredientTypeDto toResponse(IngredientType entity) {
        return toResponse(entity, entity.getName());
    }

    public IngredientTypeDto toResponse(IngredientType entity, String name) {
        return new IngredientTypeDto(
                entity.getId(),
                name
        );
    }
}
