package am.shavigh.dbmigration.service;

import am.shavigh.dbmigration.repository.postgres.BibleBookChaptersRepo;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ChapterMigrationService {

    private static final Logger log = LoggerFactory.getLogger(MigrationService.class);

    private final WebScrapingService webScrapingService;
    private final BibleBookChaptersRepo bibleBookChaptersRepo;

    public ChapterMigrationService(WebScrapingService webScrapingService, BibleBookChaptersRepo bibleBookChaptersRepo) {
        this.webScrapingService = webScrapingService;
        this.bibleBookChaptersRepo = bibleBookChaptersRepo;
    }

    @Transactional
    public void migrate(String postURL, String translationName) {
        substituteChapterContent(postURL, translationName, 1);
    }

    private void substituteChapterContent(String postURL, String translationName, int counter) {
        var nextLink = webScrapingService.getNextLinkWithSelenium(postURL);
        var extract = webScrapingService.getBreadcrumbsAndContentWithSelenium(postURL);

        if (extract.getContentHtml().isBlank()) {
            throw new RuntimeException("extract content is blank for post url " + postURL);
        }

        if (extract.getBreadcrumbs().isBlank()) {
            throw new RuntimeException("extract breadcrumbs is blank for post url " + postURL);
        }

        var urlArmenian = extract.getBreadcrumbs().toLowerCase()
                .replaceAll("\\s+", "")
                .trim();
        var segment1 = extractSegment1(urlArmenian);
        var segment2 = extractSegment2(urlArmenian);

        if (segment1 == null || segment1.isBlank()) {
            throw new RuntimeException("segment 1 is null or blank");
        }

        if (segment2 == null || segment2.isBlank()) {
            throw new RuntimeException("segment 2 is null or blank");
        }

        log.info("{} : ARMENIAN URL = {} , segment1 - {}, segment2 - {}", counter, urlArmenian, segment1, segment2);
        var chapters = bibleBookChaptersRepo.findByUrlArmenianContainingTwoSegmentsAndTranslation(segment1, segment2, translationName);

        if (chapters.size() != 1) {
            throw new RuntimeException("Not unique result :: " + urlArmenian);
        }

        var chapter = chapters.getFirst();

        if (chapter == null || chapter.getContent().isBlank()) {
            throw new RuntimeException("Check book chapter by url_armenian :: " + urlArmenian);
        }

        if (!chapter.isUnexpectedLink()) {
            chapter.setContent(extract.getContentHtml());
            chapter.setUrlArmenian(urlArmenian);
            bibleBookChaptersRepo.save(chapter);
        }


        if (nextLink != null && !nextLink.isBlank()) {
            counter++;
            substituteChapterContent(nextLink, translationName, counter);
        }
    }

    public static String extractSegment1(String s) {
        if (s == null) return null;

        int first = s.indexOf('/');
        if (first < 0) return null;

        int second = s.indexOf('/', first + 1);
        if (second < 0) return null;

        int start = second + 1;                    // at "հինկտակարան"
        int dot = s.indexOf('.', start);           // first '.' after that
        if (dot < 0) return null;

        return s.substring(start, dot + 1);        // "հինկտակարան/1."
    }

    /**
     * e.g. ".../գլուխ1" -> "գլուխ1" (ignores trailing slash)
     */
    public static String extractSegment2(String s) {
        if (s == null) return null;

        int end = s.length();
        // ignore trailing slashes
        while (end > 0 && s.charAt(end - 1) == '/') end--;
        if (end == 0) return "";

        int lastSlash = s.lastIndexOf('/', end - 1);
        return s.substring(lastSlash + 1, end);    // "գլուխ1"
    }


}
