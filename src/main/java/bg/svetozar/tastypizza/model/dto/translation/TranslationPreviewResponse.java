package bg.svetozar.tastypizza.model.dto.translation;

import java.util.Map;

public record TranslationPreviewResponse(
        String entityType,
        String sourceLanguage,
        Map<String, Map<String, TranslationPreviewValueDto>> fields
) {
}
