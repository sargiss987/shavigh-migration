package am.shavigh.dbmigration.service;

import am.shavigh.dbmigration.model.postgres.BibleBookChapterPages;
import am.shavigh.dbmigration.repository.postgres.BibleBookChapterPagesRepo;
import am.shavigh.dbmigration.repository.postgres.BibleBookChaptersRepo;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CleaningService {

    private static final Logger log = LoggerFactory.getLogger(CleaningService.class);

    private final BibleBookChaptersRepo bibleBookChaptersRepo;
    private final BibleBookChapterPagesRepo bibleBookChapterPagesRepo;


    public CleaningService(BibleBookChaptersRepo bibleBookChaptersRepo, BibleBookChapterPagesRepo bibleBookChapterPagesRepo) {
        this.bibleBookChaptersRepo = bibleBookChaptersRepo;
        this.bibleBookChapterPagesRepo = bibleBookChapterPagesRepo;
    }

    @Transactional
    public void cleanPages() {
        var pages = bibleBookChapterPagesRepo.findByHasNestedLinksTrue();
        log.info("Found {} pages with hasNestedLinks = true", pages.size());

        int updatedCount = 0;

        for (var page : pages) {
            String oldContent = page.getContent();
            String newContent = cleanContent(oldContent);

            if (!oldContent.equals(newContent)) {
                page.setContent(newContent);
                bibleBookChapterPagesRepo.save(page);
                updatedCount++;
                log.info("Updated page ID: {}", page.getId());
            } else {
                log.debug("No change for page ID: {}", page.getId());
            }
        }

        log.info("Cleaning completed. Total pages updated: {}", updatedCount);
    }

    private String cleanContent(String content) {
        var pattern = Pattern.compile("<a\\s+[^>]*?href=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
        var matcher = pattern.matcher(content);

        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String originalHref = matcher.group(1);
            String lastPart = extractLastPart(originalHref);
            log.info("Found link href: {}", originalHref);
            log.info("Extracted last part: {}", lastPart);

            // Use list result to handle multiple matches
            var matches = bibleBookChapterPagesRepo.findAllByOldUniqueName(lastPart);

            String newUrl = null;
            if (matches.isEmpty()) {
                log.warn("No URL found in DB for oldUniqueName '{}', keeping original href '{}'", lastPart, originalHref);
            } else {
                newUrl = matches.get(0).getUrl();
                if (matches.size() > 1) {
                    log.warn("Multiple matches found for oldUniqueName '{}', using first URL '{}'", lastPart, newUrl);
                } else {
                    log.info("Single match found for oldUniqueName '{}', URL '{}'", lastPart, newUrl);
                }
            }

            if (newUrl != null) {
                String updatedATag = matcher.group(0).replace(originalHref, newUrl);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(updatedATag));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }


    private String extractLastPart(String href) {
        if (href == null || href.isEmpty()) {
            return "";
        }

        // Remove fragment or query string
        int qIndex = href.indexOf('?');
        int hIndex = href.indexOf('#');
        int cutIndex = (qIndex == -1) ? href.length() : qIndex;
        cutIndex = (hIndex == -1) ? cutIndex : Math.min(cutIndex, hIndex);

        String cleanHref = href.substring(0, cutIndex);

        // Remove trailing slash if present
        if (cleanHref.endsWith("/")) {
            cleanHref = cleanHref.substring(0, cleanHref.length() - 1);
        }

        // Extract last part after last '/'
        int lastSlash = cleanHref.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash < cleanHref.length() - 1) {
            return cleanHref.substring(lastSlash + 1);
        }

        return cleanHref;  // fallback if no slash or single part
    }




    // --------------------------------------------------------------------------------





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

