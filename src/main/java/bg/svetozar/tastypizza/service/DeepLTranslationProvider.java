package bg.svetozar.tastypizza.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

@Slf4j
public class DeepLTranslationProvider implements TranslationProvider {

    private static final Map<String, String> LANGUAGE_CODES = Map.of(
            "en", "EN",
            "bg", "BG",
            "de", "DE",
            "fr", "FR"
    );

    private final DeepLTextClient client;

    public DeepLTranslationProvider(String apiKey) {
        this(new OfficialDeepLTextClient(apiKey));
    }

    DeepLTranslationProvider(DeepLTextClient client) {
        this.client = client;
    }

    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        if (!StringUtils.hasText(text)) {
            return "";
        }

        String source = normalizeLanguage(sourceLanguage);
        String target = normalizeLanguage(targetLanguage);

        if (source == null || target == null) {
            log.warn("DeepL translation skipped because language is unsupported. source={} target={}", sourceLanguage, targetLanguage);
            return text;
        }

        if (source.equals(target)) {
            return text;
        }

        try {
            return client.translateText(text, source, target);
        } catch (Exception ex) {
            log.warn("DeepL translation failed. source={} target={}", source, target, ex);
            return text;
        }
    }

    private String normalizeLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            return null;
        }
        return LANGUAGE_CODES.get(language.trim().toLowerCase(Locale.ROOT));
    }
}
