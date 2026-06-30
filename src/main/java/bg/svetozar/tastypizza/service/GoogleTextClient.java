package bg.svetozar.tastypizza.service;

interface GoogleTextClient {

    String translateText(String text, String sourceLanguage, String targetLanguage) throws Exception;
}
