package am.shavigh.dbmigration.service;

import am.shavigh.dbmigration.dto.MigrationResult;
import am.shavigh.dbmigration.model.mysql.Post;
import am.shavigh.dbmigration.model.postgres.BibleBookChapters;
import am.shavigh.dbmigration.model.postgres.BibleBooks;
import am.shavigh.dbmigration.repository.mysql.MysqlRepo;
import am.shavigh.dbmigration.repository.postgres.BibleBookChaptersRepo;
import am.shavigh.dbmigration.repository.postgres.BibleBookRepo;
import am.shavigh.dbmigration.repository.postgres.BibleRepo;
import am.shavigh.dbmigration.repository.postgres.BibleTranslationRepo;
import am.shavigh.dbmigration.util.MigrationValidator;
import am.shavigh.dbmigration.util.URLUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class MigrationService {

    private static final Logger log = LoggerFactory.getLogger(MigrationService.class);
    private final MysqlRepo mysqlRepo;
    private final BibleBookRepo bibleBookRepo;
    private final BibleRepo bibleRepo;
    private final BibleTranslationRepo bibleTranslationRepo;
    private final BibleBookChaptersRepo bibleBookChaptersRepo;
    private final WebScrapingService webScrapingService;
    private final TranslationService translationService;

    public MigrationService(MysqlRepo mysqlRepo, BibleBookRepo bibleBookRepo, BibleRepo bibleRepo, BibleTranslationRepo bibleTranslationRepo, BibleBookChaptersRepo bibleBookChaptersRepo, WebScrapingService webScrapingService, TranslationService translationService) {
        this.mysqlRepo = mysqlRepo;
        this.bibleBookRepo = bibleBookRepo;
        this.bibleRepo = bibleRepo;
        this.bibleTranslationRepo = bibleTranslationRepo;
        this.bibleBookChaptersRepo = bibleBookChaptersRepo;
        this.webScrapingService = webScrapingService;
        this.translationService = translationService;
    }

    @Transactional
    public MigrationResult migrate(String postURL, String bookName, int serialNumber, int bibleBookId) {
        var result = new MigrationResult();
        try {

            // create and save books
            var bibleBooksMap = createAndSaveBooks(bookName, serialNumber, bibleBookId);

            // recursively create and save book's chapters
            var iterationsCount = 0;
            var allChapters = new ArrayList<BibleBookChapters>();
            createAndSaveBookChapters(bibleBooksMap, postURL, result, iterationsCount, allChapters);
            setPrevAndNextLinks(allChapters);
            bibleBookChaptersRepo.saveAll(allChapters);

            result.setSuccessMessage("Migration successful, " + iterationsCount + " iterations");
            return result;

        } catch (Exception ex) {
            log.error("Migration failed : {}", ex.getMessage(), ex);
            result.setErrorMessage("Migration failed : " + ex.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return result;
        }
    }

    private HashMap<Long, BibleBooks> createAndSaveBooks(String bookName, int serialNumber, int bibleBookId) {
        var bookTitles = new ArrayList<>(Arrays.stream(bookName.split(",")).toList());
        var bookTitleRu = translationService.translateText(bookTitles.get(1), "hy", "ru");
        bookTitles.add(bookTitleRu);

        var bible = bibleRepo.findById((long) bibleBookId)
                .orElseThrow(() -> new RuntimeException("Bible not found"));
        var translationId = new AtomicInteger(1);

        var bibleBooksMap = new HashMap<Long, BibleBooks>();

        var books = bookTitles.stream().map(title -> {
            var bibleTranslation = bibleTranslationRepo.findById((long) translationId.getAndIncrement())
                    .orElseThrow(() -> new RuntimeException("Bible translation not found"));
            var book = new BibleBooks();
            book.setBible(bible);
            book.setTitle(title);
            book.setSerialNumber(serialNumber);
            book.setBibleTranslation(bibleTranslation);
            bibleBooksMap.put(bibleTranslation.getId(), book);
            return book;
        }).toList();

        bibleBookRepo.saveAll(books);

        return bibleBooksMap;
    }

    private void createAndSaveBookChapters(HashMap<Long, BibleBooks> bibleBooksMap, String postURL, MigrationResult result, int iterationsCount, List<BibleBookChapters> allChapters) {
        var postName = URLUtil.extractSegmentBetweenLastTwoSlashes(postURL);
        MigrationValidator.validatePostName(postName, result);

        var postList = mysqlRepo.findByPostName(postName);
        MigrationValidator.validatePost(postList);

        var breadcrumbs = webScrapingService.getBreadcrumbsWithSelenium(postURL);
        var url = translationService.translateText(breadcrumbs, "hy", "en");
        var bookChapters = migrateBibleBookChapters(bibleBooksMap, postList.getFirst(), url, iterationsCount);
        allChapters.addAll(bookChapters);
        iterationsCount++;

        var nextLink = webScrapingService.getNextLinkWithSelenium(postURL);
        if (nextLink != null && !nextLink.isBlank()) {
            createAndSaveBookChapters(bibleBooksMap, nextLink, result, iterationsCount, allChapters);
        }

    }

    private List<BibleBookChapters> migrateBibleBookChapters(HashMap<Long, BibleBooks> bibleBooksMap, Post post, String url, int iterationsCount) {
        var content = post.getPostContent();
        var bookChaptersContents = splitByLanguageSections(content);
        String title = post.getPostTitle().contains("Ներածութիւն") ? "Ներածութիւն" : String.valueOf(iterationsCount);

        String baseTranslation = "Echmiadzin Translation";
        var urlMap = Map.of(
                1L, url,
                2L, url.replace(baseTranslation, "Ararat translation"),
                3L, url.replace(baseTranslation, "Grabar translation"),
                4L, url.replace(baseTranslation, "Russian translation")
        );

        var bibleBookChaptersList = new ArrayList<BibleBookChapters>();
        for (var entry : bookChaptersContents.entrySet()) {
            var languageId = entry.getKey();
            var chapterContent = entry.getValue();
            var bibleBookChapter = new BibleBookChapters();
            bibleBookChapter.setBibleBook(bibleBooksMap.get(languageId));
            bibleBookChapter.setUrl(urlMap.get(languageId));
            bibleBookChapter.setTitle(title);

            if (title.equals("Ներածութիւն") && languageId != 1L) {
                bibleBookChapter.setLinkToDefaultContent(urlMap.get(1L));
            }

            if (languageId == 1L) {
                bibleBookChapter.setOldUniqueName(post.getPostName());
                //create subchapters

                // modify content a tag links
            } else {
                bibleBookChapter.setContent(chapterContent);
            }

            bibleBookChaptersList.add(bibleBookChapter);
        }

        return  bibleBookChaptersList;
    }

    public static Map<Long, String> splitByLanguageSections(String content) {
        if (content == null || content.isBlank()) {
            throw new RuntimeException("Post: Empty content");
        }

        var languageMap = Map.of(
                "[:en]", 1L,
                "[:ca]", 2L,
                "[:gl]", 3L,
                "[:ru]", 4L
        );

        var markers = List.of("[:en]", "[:ca]", "[:gl]", "[:ru]", "[:]");
        var sections = new HashMap<Long, String>();

        boolean isPrefaceOnly = !content.contains("[:ca]") && !content.contains("[:gl]") && !content.contains("[:ru]");

        if (isPrefaceOnly) {
            int startIdx = content.indexOf("[:en]") + "[:en]".length();
            int endIdx = content.indexOf("[:]");

            if (startIdx < 0 || endIdx < startIdx) {
                throw new RuntimeException("Invalid format for preface content");
            }

            sections.put(1L, content.substring(startIdx, endIdx).trim());
            sections.put(2L, "Տվյալ նյութը հասանելի չէ այս թարգմանությամբ: Այն կարող եք դիտել Այստեղ.");
            sections.put(3L, "Տվյալ նյութը հասանելի չէ այս թարգմանությամբ: Այն կարող եք դիտել Այստեղ.");
            sections.put(4L, "Данный материал недоступен на этом языке. Вы можете просмотреть его здесь.");
            return sections;
        }

        for (int i = 0; i < markers.size() - 1; i++) {
            String startMarker = markers.get(i);
            String endMarker = markers.get(i + 1);

            var startIdx = content.indexOf(startMarker);
            var endIdx = content.indexOf(endMarker);

            var key = languageMap.get(startMarker);
            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                String section = content.substring(startIdx + startMarker.length(), endIdx).trim();
                sections.put(key, section);
            } else {
                sections.putIfAbsent(key, "Տվյալ նյութը հասանելի չէ այս թարգմանությամբ: Այն կարող եք դիտել Այստեղ.");
            }
        }

        return sections;
    }

    private static void setPrevAndNextLinks(ArrayList<BibleBookChapters> bibleBookChaptersList) {
        Map<Long, List<BibleBookChapters>> groupedByTranslation = bibleBookChaptersList.stream()
                .collect(Collectors.groupingBy(ch -> ch.getBibleBook().getBibleTranslation().getId()));


        for (var entry : groupedByTranslation.entrySet()) {
            List<BibleBookChapters> chapters = entry.getValue();


            chapters.sort(Comparator.comparingInt(ch -> {
                String title = ch.getTitle();
                if ("Ներածութիւն".equals(title)) return 0;
                try {
                    return Integer.parseInt(title);
                } catch (NumberFormatException e) {
                    return Integer.MAX_VALUE;
                }
            }));

            for (int i = 0; i < chapters.size(); i++) {
                var current = chapters.get(i);

                if (i > 0) {
                    current.setPrevLink(chapters.get(i - 1).getUrl());
                }

                if (i < chapters.size() - 1) {
                    current.setNextLink(chapters.get(i + 1).getUrl());
                }
            }
        }
    }
}
