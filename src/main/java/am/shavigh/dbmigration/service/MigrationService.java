package am.shavigh.dbmigration.service;

import am.shavigh.dbmigration.dto.MigrationResult;
import am.shavigh.dbmigration.model.mysql.Post;
import am.shavigh.dbmigration.model.postgres.BibleBook;
import am.shavigh.dbmigration.repository.mysql.MysqlRepo;
import am.shavigh.dbmigration.repository.postgres.PostgresRepo;
import am.shavigh.dbmigration.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MigrationService {

    private static final Logger log = LoggerFactory.getLogger(MigrationService.class);
    private final PostgresRepo postgresRepo;
    private final MysqlRepo mysqlRepo;
    private final WebScrapingService webScrapingService;
    private final TranslationService translationService;

    public MigrationService(PostgresRepo postgresRepo, MysqlRepo mysqlRepo, WebScrapingService webScrapingService, TranslationService translationService) {
        this.postgresRepo = postgresRepo;
        this.mysqlRepo = mysqlRepo;
        this.webScrapingService = webScrapingService;
        this.translationService = translationService;
    }

    public MigrationResult migrate(String postURL, String bookName) {
        var result = new MigrationResult();
        try {

            var books = createBibleBooks(bookName);

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

            var nextLink = webScrapingService.fetchWebpageContent(postURL);

            if (nextLink != null && !nextLink.isBlank()) {

            }

            return result;
            
        } catch (Exception ex) {
            log.error("Migration failed : {}", ex.getMessage());
            result.setErrorMessage("Migration failed : " + ex.getMessage());
            return result;
        }
    }

    private List<BibleBook> createBibleBooks(String bookName) {
        var bookTitles = new ArrayList<>(Arrays.stream(bookName.split(",")).toList());
        var bookTitleRu = translationService.translateText(bookTitles.get(1),"hy","ru");
        bookTitles.add(bookTitleRu);

        return bookTitles.stream().map(title -> {
            var book = new BibleBook();
            book.setTitle(title);
            System.out.println("Book title : " + title);
            return book;
        }).toList();
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
