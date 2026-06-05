package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.config.TranslationProviderConfig;
import bg.svetozar.tastypizza.model.dto.translation.TranslationPreviewRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DeepLTranslationProviderTest {

    @Test
    void noApiKeyUsesFallbackProvider() {
        TranslationProviderConfig config = new TranslationProviderConfig();

        TranslationProvider disabled = config.translationProvider("", false);
        TranslationProvider missingKey = config.translationProvider(" ", true);

        assertInstanceOf(NoOpTranslationProvider.class, disabled);
        assertInstanceOf(NoOpTranslationProvider.class, missingKey);
    }

    @Test
    void sameSourceAndTargetLanguageDoesNotCallDeepL() {
        DeepLTextClient client = mock(DeepLTextClient.class);
        DeepLTranslationProvider provider = new DeepLTranslationProvider(client);

        String result = provider.translate("Original text", "en", "en");

        assertEquals("Original text", result);
        verifyNoInteractions(client);
    }

    @Test
    void deepLProviderReturnsTranslatedText() throws Exception {
        DeepLTextClient client = mock(DeepLTextClient.class);
        when(client.translateText("Hello", "EN", "FR")).thenReturn("Bonjour");
        DeepLTranslationProvider provider = new DeepLTranslationProvider(client);

        String result = provider.translate("Hello", "en", "fr");

        assertEquals("Bonjour", result);
        verify(client).translateText("Hello", "EN", "FR");
    }

    @Test
    void translationPreviewDoesNotDependOnPersistenceServices() {
        TranslationPreviewService service = new TranslationPreviewService(new NoOpTranslationProvider());

        var response = service.preview(new TranslationPreviewRequest(
                "PRODUCT",
                "en",
                List.of("bg"),
                Map.of("name", Map.of("en", "Margherita"))
        ));

        assertEquals("Margherita", response.fields().get("name").get("bg").translatedText());
        for (Field field : TranslationPreviewService.class.getDeclaredFields()) {
            assertFalse(field.getType().getSimpleName().contains("Repository"));
            assertFalse(field.getType().getSimpleName().equals("TranslationService"));
        }
    }
}
