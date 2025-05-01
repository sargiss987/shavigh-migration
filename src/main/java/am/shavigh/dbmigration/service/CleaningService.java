package am.shavigh.dbmigration.service;

import am.shavigh.dbmigration.repository.postgres.BibleBookChaptersRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CleaningService {

    private final BibleBookChaptersRepo bibleBookChaptersRepo;

    public CleaningService(BibleBookChaptersRepo bibleBookChaptersRepo) {
        this.bibleBookChaptersRepo = bibleBookChaptersRepo;
    }

    @Transactional
    public void clean() {
        var chapters = bibleBookChaptersRepo.findAll();

        chapters.forEach(chapter -> {
            chapter.setUrl(cleanUrl(chapter.getUrl()));
            if (chapter.getPrevLink() != null) {
                chapter.setPrevLink(cleanUrl(chapter.getPrevLink()));
            }
            if (chapter.getNextLink() != null) {
                chapter.setNextLink(cleanUrl(chapter.getNextLink()));
            }
            if (chapter.getLinkToDefaultContent() != null) {
                chapter.setLinkToDefaultContent(cleanUrl(chapter.getLinkToDefaultContent()));
            }
        });

        bibleBookChaptersRepo.saveAll(chapters);
    }

    private static String cleanUrl(String url) {
        url = cleanPenultimateSegment(url);
        url = simplifyTranslationChunk(url);
        return url;
    }

    public static String cleanPenultimateSegment(String url) {
        String[] parts = url.split("/");

        if (parts.length < 2) return url;

        int penultimateIndex = parts.length - 2;

        parts[penultimateIndex] = parts[penultimateIndex]
                .replaceFirst("^\\d+(\\.\\d+)?\\.", "")  // e.g. 13.1chronicles → chronicles
                .replaceFirst("^\\d+", "");              // e.g. 2ezra → ezra

        return String.join("/", parts);
    }

    public static String simplifyTranslationChunk(String url) {
        String[] parts = url.split("/");

        if (parts.length >= 2 && parts[1].endsWith("translation")) {
            parts[1] = parts[1].replace("translation", ""); // e.g. russiantranslation → russian
        }

        return String.join("/", parts);
    }
}

