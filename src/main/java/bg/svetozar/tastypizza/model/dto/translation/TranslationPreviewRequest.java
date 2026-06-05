package bg.svetozar.tastypizza.model.dto.translation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record TranslationPreviewRequest(
        @NotBlank(message = "Entity type is required")
        String entityType,

        @NotBlank(message = "Source language is required")
        String sourceLanguage,

        @NotEmpty(message = "Target languages are required")
        List<@NotBlank(message = "Target language is required") String> targetLanguages,

        @NotNull(message = "Fields are required")
        @NotEmpty(message = "Fields are required")
        Map<String, Map<String, String>> fields
) {
}
