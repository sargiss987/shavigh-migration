package am.shavigh.dbmigration.config;


import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.cloud.translate.v3.TranslationServiceSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class GoogleTranslateConfig {

    @Bean
    public TranslationServiceClient translationServiceClient(@Value("${app.translation.auth-file-path}") String pathToJson) throws IOException {

        var settings = TranslationServiceSettings.newBuilder()
                .setCredentialsProvider(() -> ServiceAccountCredentials.fromStream(new FileInputStream(pathToJson)))
                .build();

        return TranslationServiceClient.create(settings);
    }
}
