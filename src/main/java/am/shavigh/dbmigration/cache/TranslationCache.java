package am.shavigh.dbmigration.cache;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TranslationCache {
    private final Path filePath = Paths.get("translation-cache.ser");
    private final Map<TranslationKey, String> cache;

    public TranslationCache() {
        this.cache = loadCacheFromFile();
    }

    public synchronized String get(TranslationKey key) {
        return cache.get(key);
    }

    public synchronized void put(TranslationKey key, String value) {
        cache.put(key, value);
        saveCacheToFile();
    }

    private Map<TranslationKey, String> loadCacheFromFile() {
        if (Files.exists(filePath)) {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
                return (Map<TranslationKey, String>) ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ConcurrentHashMap<>();
    }

    private void saveCacheToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filePath))) {
            oos.writeObject(cache);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

