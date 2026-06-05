package bg.svetozar.tastypizza.service;

interface DeepLTextClient {

    String translateText(String text, String sourceLanguage, String targetLanguage) throws Exception;
}
