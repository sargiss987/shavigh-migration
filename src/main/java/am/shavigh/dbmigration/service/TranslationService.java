package am.shavigh.dbmigration.service;

import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslationServiceClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    // Replace with your project ID
    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);

    private final TranslationServiceClient translationServiceClient;

    public TranslationService(TranslationServiceClient translationServiceClient) {
        this.translationServiceClient = translationServiceClient;
    }

    public String translateText(String text, String sourceLang, String targetLang) {
        var parent = LocationName.of("directed-potion-456709-k3", "global");

        var request = TranslateTextRequest.newBuilder()
                .setParent(parent.toString())
                .setMimeType("text/plain")
                .addContents(text)
                .setSourceLanguageCode(sourceLang)
                .setTargetLanguageCode(targetLang)
                .build();

        var response = translationServiceClient.translateText(request);

        var result =  response.getTranslationsCount() > 0
                ? response.getTranslations(0).getTranslatedText()
                : "";

        log.info("Translated text: {}", result);
        return result;
    }
}
