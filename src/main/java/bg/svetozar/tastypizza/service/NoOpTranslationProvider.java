package bg.svetozar.tastypizza.service;

public class NoOpTranslationProvider implements TranslationProvider {

    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        return text == null ? "" : text;
    }
}
