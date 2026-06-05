package bg.svetozar.tastypizza.model.dto.translation;

import java.util.Map;

public record EntityTranslationsResponse(
        String entityType,
        Long entityId,
        Map<String, Map<String, TranslationValueDto>> fields
) {
}
