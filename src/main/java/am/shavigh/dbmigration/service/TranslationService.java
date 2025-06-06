package am.shavigh.dbmigration.service;

import am.shavigh.dbmigration.cache.TranslationCache;
import am.shavigh.dbmigration.cache.TranslationKey;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslationServiceClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);

    private final TranslationCache cache;
    private final TranslationServiceClient translationServiceClient;

    public TranslationService(TranslationCache cache,
                              TranslationServiceClient translationServiceClient) {
        this.cache = cache;
        this.translationServiceClient = translationServiceClient;
    }

    public String translateText(String text, String sourceLang, String targetLang) {
        TranslationKey key = new TranslationKey(text, sourceLang, targetLang);
        String cached = cache.get(key);
        if (cached != null) {
            log.info("Cache hit for text: {}", text);
            return cached;
        }

        var request = TranslateTextRequest.newBuilder()
                .setParent(LocationName.of("directed-potion-456709-k3", "global").toString())
                .setMimeType("text/plain")
                .addContents(text)
                .setSourceLanguageCode(sourceLang)
                .setTargetLanguageCode(targetLang)
                .build();

        log.info("Translating text via API: {}", text);
        var response = translationServiceClient.translateText(request);
        var result = response.getTranslationsCount() > 0
                ? response.getTranslations(0).getTranslatedText()
                : "";

        cache.put(key, result);
        return result;
    }
}
