package bg.svetozar.tastypizza.model.dto.ingredient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record IngredientRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2,max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotNull(message = "Type id is required")
        @Positive(message = "Type id must be positive")
        Long typeId
) {}
