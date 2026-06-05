package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.model.dto.translation.TranslationPreviewRequest;
import bg.svetozar.tastypizza.model.dto.translation.TranslationPreviewResponse;
import bg.svetozar.tastypizza.model.dto.translation.TranslationPreviewValueDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TranslationPreviewService {

    private final TranslationProvider translationProvider;

    public TranslationPreviewResponse preview(TranslationPreviewRequest request) {
        Map<String, Map<String, TranslationPreviewValueDto>> translatedFields = new LinkedHashMap<>();

        request.fields().forEach((fieldName, languageValues) -> {
            Map<String, TranslationPreviewValueDto> fieldTranslations = new LinkedHashMap<>();
            String sourceText = resolveSourceText(languageValues, request.sourceLanguage());

            request.targetLanguages().forEach(targetLanguage -> {
                String existingValue = languageValues != null ? languageValues.get(targetLanguage) : null;

                if (StringUtils.hasText(existingValue)) {
                    fieldTranslations.put(
                            targetLanguage,
                            new TranslationPreviewValueDto(existingValue, false)
                    );
                    return;
                }

                String generatedText = translationProvider.translate(
                        sourceText,
                        request.sourceLanguage(),
                        targetLanguage
                );

                fieldTranslations.put(
                        targetLanguage,
                        new TranslationPreviewValueDto(generatedText, true)
                );
            });

            translatedFields.put(fieldName, fieldTranslations);
        });

        return new TranslationPreviewResponse(
                request.entityType(),
                request.sourceLanguage(),
                translatedFields
        );
    }

    private String resolveSourceText(Map<String, String> languageValues, String sourceLanguage) {
        if (languageValues == null || languageValues.isEmpty()) {
            return "";
        }

        String sourceText = languageValues.get(sourceLanguage);
        if (StringUtils.hasText(sourceText)) {
            return sourceText;
        }

        return languageValues.values().stream()
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("");
    }
}
