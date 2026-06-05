package bg.svetozar.tastypizza.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LocalizedTextService {

    public static final String LANGUAGE_EN = "en";

    private final TranslationService translationService;

    public String resolveEnglishField(
            Map<String, Map<String, String>> translations,
            Map<String, Map<String, String>> fields,
            String fieldName,
            String legacyValue
    ) {
        Map<String, Map<String, String>> values = translationFields(translations, fields);
        if (values != null) {
            Map<String, String> fieldValues = values.get(fieldName);
            if (fieldValues != null && StringUtils.hasText(fieldValues.get(LANGUAGE_EN))) {
                return fieldValues.get(LANGUAGE_EN).trim();
            }
        }

        return legacyValue != null ? legacyValue.trim() : null;
    }

    @Transactional
    public void saveTranslations(
            String entityType,
            Long entityId,
            Map<String, Map<String, String>> translations,
            Map<String, Map<String, String>> fields,
            Map<String, String> englishDefaults
    ) {
        if (entityId == null) {
            return;
        }

        Map<String, Map<String, String>> source = translationFields(translations, fields);
        Map<String, Map<String, String>> merged = new LinkedHashMap<>();
        if (source != null) {
            source.forEach((fieldName, languageValues) ->
                    merged.put(fieldName, languageValues == null ? new LinkedHashMap<>() : new LinkedHashMap<>(languageValues))
            );
        }

        if (englishDefaults != null) {
            englishDefaults.forEach((fieldName, value) ->
                    merged.computeIfAbsent(fieldName, key -> new LinkedHashMap<>()).put(LANGUAGE_EN, value)
            );
        }

        merged.forEach((fieldName, languageValues) ->
                languageValues.forEach((language, translatedText) -> {
                    if (StringUtils.hasText(language) && translatedText != null) {
                        translationService.saveOrUpdateTranslation(
                                entityType,
                                entityId,
                                fieldName,
                                normalizeLanguage(language),
                                translatedText,
                                false
                        );
                    }
                })
        );
    }

    @Transactional(readOnly = true)
    public String getTranslationOrDefault(
            String entityType,
            Long entityId,
            String fieldName,
            String language,
            String defaultText
    ) {
        String normalizedLanguage = normalizeLanguage(language);
        if (!StringUtils.hasText(normalizedLanguage) || LANGUAGE_EN.equals(normalizedLanguage)) {
            return defaultText;
        }

        return translationService.getTranslationOrDefault(
                entityType,
                entityId,
                fieldName,
                normalizedLanguage,
                defaultText
        );
    }

    private Map<String, Map<String, String>> translationFields(
            Map<String, Map<String, String>> translations,
            Map<String, Map<String, String>> fields
    ) {
        if (translations != null && !translations.isEmpty()) {
            return translations;
        }
        return fields;
    }

    private String normalizeLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            return null;
        }
        return language.trim().toLowerCase(Locale.ROOT);
    }
}
