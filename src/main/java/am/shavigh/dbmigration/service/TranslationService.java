package am.shavigh.dbmigration.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
public class TranslationService {

    private static final String API_URL = "https://translate.google.com/?sl=hy&tl=ru&op=translate";
    private final RestTemplate restTemplate;

    public TranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String translateText(String text, String srcLanguage, String targetLanguage) {
        var requestBody = new HashMap<>();
        requestBody.put("q", text);
        requestBody.put("source", srcLanguage);
        requestBody.put("target", targetLanguage);
        requestBody.put("format", "text");

        return restTemplate.postForObject(API_URL, requestBody, String.class);
    }
}
