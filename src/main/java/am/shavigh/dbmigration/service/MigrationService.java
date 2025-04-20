package am.shavigh.dbmigration.service;

import am.shavigh.dbmigration.dto.MigrationResult;
import am.shavigh.dbmigration.model.mysql.Post;
import am.shavigh.dbmigration.model.postgres.BibleBooks;
import am.shavigh.dbmigration.repository.mysql.MysqlRepo;
import am.shavigh.dbmigration.repository.postgres.BibleBookRepo;
import am.shavigh.dbmigration.repository.postgres.BibleRepo;
import am.shavigh.dbmigration.repository.postgres.BibleTranslationRepo;
import am.shavigh.dbmigration.util.URLUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MigrationService {

    private static final Logger log = LoggerFactory.getLogger(MigrationService.class);
    private final MysqlRepo mysqlRepo;
    private final BibleBookRepo bibleBookRepo;
    private final BibleRepo bibleRepo;
    private final BibleTranslationRepo bibleTranslationRepo;
    private final WebScrapingService webScrapingService;
    private final TranslationService translationService;

    public MigrationService(MysqlRepo mysqlRepo, BibleBookRepo bibleBookRepo, BibleRepo bibleRepo, BibleTranslationRepo bibleTranslationRepo, WebScrapingService webScrapingService, TranslationService translationService) {
        this.mysqlRepo = mysqlRepo;
        this.bibleBookRepo = bibleBookRepo;
        this.bibleRepo = bibleRepo;
        this.bibleTranslationRepo = bibleTranslationRepo;
        this.webScrapingService = webScrapingService;
        this.translationService = translationService;
    }

    @Transactional
    public MigrationResult migrate(String postURL, String bookName, int serialNumber, int bibleBookId) {
        var result = new MigrationResult();
        try {

            // create and save books
            createAndSaveBooks(bookName, serialNumber, bibleBookId);


            // recursively create and save book's chapters
            var postName = URLUtil.extractSegmentBetweenLastTwoSlashes(postURL);
            validatePostName(postName, result);

            var postList = mysqlRepo.findByPostName(postName);
            if (postList.size() > 1){
                result.setErrorMessage("Not unique result");
            } else if (postList.isEmpty()) {
                result.setErrorMessage("Empty result");
            } else {
                migrateBibleBookChapters(postList.getFirst(), result);
            }

            var nextLink = webScrapingService.getNextLinkWithSelenium(postURL);
            var breadcrumbs = webScrapingService.getBreadcrumbsWithSelenium(postURL);
            System.out.println("Next link: " + nextLink);
            System.out.println("Breadcrumbs: " + breadcrumbs);

            if (nextLink != null && !nextLink.isBlank()) {

            }

            return result;
            
        } catch (Exception ex) {
            log.error("Migration failed : {}", ex.getMessage());
            result.setErrorMessage("Migration failed : " + ex.getMessage());
            return result;
        }
    }

    private void createAndSaveBooks(String bookName, int serialNumber, int bibleBookId) {
        var bookTitles = new ArrayList<>(Arrays.stream(bookName.split(",")).toList());
        var bookTitleRu = translationService.translateText(bookTitles.get(1), "hy", "ru");
        bookTitles.add(bookTitleRu);

        var bible = bibleRepo.findById((long) bibleBookId)
                .orElseThrow(() -> new RuntimeException("Bible not found"));
        var translationId = new AtomicInteger(1);

        var books = bookTitles.stream().map(title -> {
            var bibleTranslation = bibleTranslationRepo.findById((long) translationId.getAndIncrement())
                    .orElseThrow(() -> new RuntimeException("Bible translation not found"));
            var book = new BibleBooks();
            book.setBible(bible);
            book.setTitle(title);
            book.setSerialNumber(serialNumber);
            book.setBibleTranslation(bibleTranslation); // Uncomment if needed
            return book;
        }).toList();

        bibleBookRepo.saveAll(books);
    }

    private static void validatePostName(String postName, MigrationResult result) {
        if (postName.isEmpty()) {
            result.setErrorMessage("Empty post name");
            log.info("Post name : {}", postName);
            throw new RuntimeException("Empty post name");
        }
    }

    private void migrateBibleBookChapters(Post first, MigrationResult result) {
        var content = first.getPostContent();

        result.setSuccessMessage(content);
    }

}
