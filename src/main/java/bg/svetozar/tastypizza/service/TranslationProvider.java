package bg.svetozar.tastypizza.service;

public interface TranslationProvider {

    String translate(String text, String sourceLanguage, String targetLanguage);
}
