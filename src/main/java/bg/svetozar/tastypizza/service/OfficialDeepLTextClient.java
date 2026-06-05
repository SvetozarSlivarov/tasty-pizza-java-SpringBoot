package bg.svetozar.tastypizza.service;

import com.deepl.api.DeepLClient;

class OfficialDeepLTextClient implements DeepLTextClient {

    private final DeepLClient client;

    OfficialDeepLTextClient(String apiKey) {
        this.client = new DeepLClient(apiKey);
    }

    @Override
    public String translateText(String text, String sourceLanguage, String targetLanguage) throws Exception {
        return client.translateText(text, sourceLanguage, targetLanguage).getText();
    }
}
