package bg.svetozar.tastypizza.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

class OfficialGoogleTranslateTextClient implements GoogleTextClient {

    private static final String TRANSLATE_ENDPOINT = "https://translation.googleapis.com/language/translate/v2";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    OfficialGoogleTranslateTextClient(String apiKey) {
        this(apiKey, HttpClient.newHttpClient(), new ObjectMapper());
    }

    OfficialGoogleTranslateTextClient(String apiKey, HttpClient httpClient, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String translateText(String text, String sourceLanguage, String targetLanguage) throws Exception {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(TRANSLATE_ENDPOINT)
                .queryParam("key", apiKey)
                .build()
                .toUri();

        String requestBody = objectMapper.writeValueAsString(Map.of(
                "q", text,
                "source", sourceLanguage,
                "target", targetLanguage,
                "format", "text"
        ));

        HttpRequest request = HttpRequest.newBuilder(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Google Translate API returned HTTP " + response.statusCode());
        }

        JsonNode translatedText = objectMapper.readTree(response.body())
                .path("data")
                .path("translations")
                .path(0)
                .path("translatedText");

        if (translatedText.isMissingNode() || translatedText.isNull()) {
            throw new IOException("Google Translate API response did not contain translatedText");
        }

        return translatedText.asText();
    }
}
