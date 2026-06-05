package bg.svetozar.tastypizza.config;

import bg.svetozar.tastypizza.service.DeepLTranslationProvider;
import bg.svetozar.tastypizza.service.NoOpTranslationProvider;
import bg.svetozar.tastypizza.service.TranslationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class TranslationProviderConfig {

    @Bean
    public TranslationProvider translationProvider(
            @Value("${deepl.api.key:}") String apiKey,
            @Value("${deepl.api.enabled:false}") boolean enabled
    ) {
        if (!enabled || !StringUtils.hasText(apiKey)) {
            return new NoOpTranslationProvider();
        }
        return new DeepLTranslationProvider(apiKey);
    }
}
