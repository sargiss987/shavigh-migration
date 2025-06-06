package am.shavigh.dbmigration.service;

import am.shavigh.dbmigration.dto.MigrationResult;
import am.shavigh.dbmigration.model.mysql.Post;
import am.shavigh.dbmigration.model.postgres.BibleBookChapterPages;
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

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        MigrationCheckpoint checkpoint = loadCheckpoint();
        AtomicReference<String> lastProcessedPostUrl = new AtomicReference<>(postURL);

        HashMap<Long, BibleBooks> bibleBooksMap = null;
        AtomicInteger iterationsCount = new AtomicInteger(0);
        ArrayList<BibleBookChapters> allChapters = new ArrayList<>();

        try {
            if (checkpoint != null) {
                // Resume
                log.info("Resuming migration from checkpoint...");
                bibleBooksMap = new HashMap<>(checkpoint.getBibleBooksMap());
                iterationsCount.set(checkpoint.getCurrentIteration());
                allChapters = new ArrayList<>(checkpoint.getAllChapters());
                postURL = checkpoint.getLastProcessedPostUrl();
            } else {
                // Start from scratch
                log.info("Starting new migration...");
                bibleBooksMap = createAndSaveBooks(bookName, serialNumber, bibleBookId);
                allChapters = new ArrayList<>();
            }

            createBookChapters(bibleBooksMap, postURL, result, iterationsCount, allChapters, lastProcessedPostUrl);
            setPrevAndNextLinks(allChapters);

            bibleBookChaptersRepo.saveAll(allChapters);

            // Migration successful, delete checkpoint
            boolean isDeleted = new File("migration_checkpoint.ser").delete();
            log.info("Checkpoint deleted: {}", isDeleted);

            result.setSuccessMessage("Migration successful, " + iterationsCount + " iterations");
            return result;

        } catch (Exception ex) {
            log.error("Migration failed: {}", ex.getMessage(), ex);
            result.setErrorMessage("Migration failed: " + ex.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            // Save checkpoint even on failure
            if (bibleBooksMap != null) {
                saveCheckpoint(new MigrationCheckpoint(postURL, iterationsCount.get(), allChapters, bibleBooksMap, true, lastProcessedPostUrl.get()));
            }
            return result;
        }
    }


    private void saveCheckpoint(MigrationCheckpoint checkpoint) {
        try (var oos = new ObjectOutputStream(new FileOutputStream("migration_checkpoint.ser"))) {
            oos.writeObject(checkpoint);
        } catch (IOException e) {
            log.error("Failed to save migration checkpoint", e);
        }
    }

    private MigrationCheckpoint loadCheckpoint() {
        try (var ois = new ObjectInputStream(new FileInputStream("migration_checkpoint.ser"))) {
            return (MigrationCheckpoint) ois.readObject();
        } catch (Exception e) {
            log.info("No checkpoint found or failed to load: {}", e.getMessage());
            return null;
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

    private void createBookChapters(HashMap<Long, BibleBooks> bibleBooksMap, String postURL, MigrationResult result, AtomicInteger iterationsCount, List<BibleBookChapters> allChapters, AtomicReference<String> lastProcessedPostUrl) {
        lastProcessedPostUrl.set(postURL);
        var postName = URLUtil.extractSegmentBetweenLastTwoSlashes(postURL);
        postName = MigrationValidator.validatePostName(postName, result);

        var postList = mysqlRepo.findByPostName(postName);
        MigrationValidator.validatePost(postList, postName);

        var breadcrumbs = webScrapingService.getBreadcrumbsWithSelenium(postURL);
        var url = translationService.translateText(breadcrumbs, "hy", "en")
                .replaceFirst("^Home", "bible")
                .toLowerCase()
                .replaceAll("\\s+", "")
                .trim();
        var urlArmenian = breadcrumbs.toLowerCase()
                .replaceAll("\\s+", "")
                .trim();

        var bookChapters = migrateBibleBookChapters(bibleBooksMap, postList.getFirst(), url, urlArmenian, iterationsCount, result);
        allChapters.addAll(bookChapters);
        iterationsCount.incrementAndGet();

        var nextLink = webScrapingService.getNextLinkWithSelenium(postURL);
        if (nextLink != null && !nextLink.isBlank()) {
            createBookChapters(bibleBooksMap, nextLink, result, iterationsCount, allChapters, lastProcessedPostUrl);
        }

    }

    private List<BibleBookChapters> migrateBibleBookChapters(HashMap<Long, BibleBooks> bibleBooksMap, Post post, String url, String urlArmenian, AtomicInteger iterationsCount, MigrationResult result) {
        var content = post.getPostContent();
        var bookChaptersContents = splitByLanguageSections(content);
        String title = post.getPostTitle().contains("Ներածութիւն") ? "Ներածութիւն" : String.valueOf(iterationsCount.get());

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
            bibleBookChapter.setUrlArmenian(urlArmenian);

            if (title.equals("Ներածութիւն") && languageId != 1L) {
                bibleBookChapter.setLinkToDefaultContent(urlMap.get(1L));
            }

            if (languageId == 1L) {
                bibleBookChapter.setOldUniqueName(post.getPostName());
                var nameToUrlMap = new HashMap<String, String>();
                //create pages
                var urls = URLUtil.extractUrlsFromContent(chapterContent);
                var bibleBookChapterPagesList = urls.stream()
                        .map(u -> {
                            if (u.startsWith("/")) {
                                log.info("http:// missing = {}", u);
                                u = "http://" + u.substring(1);
                                log.info("modified url = {}", u);
                            }
                            var originalName = URLUtil.extractSegmentBetweenLastTwoSlashes(u);
                            var name = MigrationValidator.validatePostName(originalName, result);

                            var bibleBookChapterPage = new BibleBookChapterPages();
                            bibleBookChapterPage.setBibleBookChapters(bibleBookChapter); // ✅ Always set parent chapter!

                            if (MigrationValidator.emptyPostUrlList.contains(name)) {
                                nameToUrlMap.put(originalName, "/404");
                                bibleBookChapterPage.setTitle("404 - Page Not Found");
                                bibleBookChapterPage.setContent("This page is not available.");
                                bibleBookChapterPage.setUrl("/404");
                                bibleBookChapterPage.setUrlArmenian("/404");
                                bibleBookChapterPage.setOldUniqueName(name);
                                return bibleBookChapterPage;
                            }

                            var page = mysqlRepo.findPageByPostName(name);
                            if (page.size() > 1) {
                                page = page.stream().filter(p -> p.getPostTitle().length() > 22).toList();
                            }

                            MigrationValidator.validatePost(page, name);

                            var uniquePage = page.getFirst();

                            var breadcrumbs = webScrapingService.getBreadcrumbsWithSelenium(u);
                            var pageUrl = translationService.translateText(breadcrumbs, "hy", "en")
                                    .replaceFirst("^Home", "bible")
                                    .toLowerCase()
                                    .replaceAll("\\s+", "")
                                    .trim();
                            var pageUrlArmenian = breadcrumbs.toLowerCase().replaceAll("\\s+", "").trim();
                            var pgContent = uniquePage.getPostContent();
                            if (!pgContent.startsWith("[:en]")) {
                                pgContent = "[:en]" + pgContent;
                            }
                            if (!pgContent.endsWith("[:]")) {
                                pgContent = pgContent + "[:]";
                            }
                            var pageContent = extractSection(pgContent);

                            nameToUrlMap.put(originalName, pageUrl);

                            var pageTitle = uniquePage.getPostTitle();
                            if (!pageTitle.startsWith("[:en]")) {
                                pageTitle = "[:en]" + pageTitle;
                            }
                            if (!pageTitle.endsWith("[:]")) {
                                pageTitle = pageTitle + "[:]";
                            }
                            bibleBookChapterPage.setTitle(extractSection(pageTitle));
                            bibleBookChapterPage.setContent(pageContent);
                            bibleBookChapterPage.setUrl(pageUrl);
                            bibleBookChapterPage.setUrlArmenian(pageUrlArmenian);
                            bibleBookChapterPage.setOldUniqueName(name);

                            var pattern = Pattern.compile("<a\\s+[^>]*href\\s*=\\s*\"http", Pattern.CASE_INSENSITIVE);
                            var matcher = pattern.matcher(pageContent);
                            if (matcher.find()) {
                                bibleBookChapterPage.setHasNestedLinks(true);
                            }

                            return bibleBookChapterPage;
                        })
                        .toList();

                // modify content a tag links
                String updatedContent = rewriteChapterContentLinks(chapterContent, nameToUrlMap);
                bibleBookChapter.setContent(updatedContent);
                bibleBookChapter.setBibleBookChapterPages(bibleBookChapterPagesList);

                // set next and prev links for pages
                setPrevAndNextLinksToPages(bibleBookChapterPagesList);

            } else {
                if (chapterContent.contains("<a")) {
                    bibleBookChapter.setUnexpectedLink(true);
                }
                bibleBookChapter.setContent(chapterContent);
            }

            bibleBookChaptersList.add(bibleBookChapter);
        }

        return bibleBookChaptersList;
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

    public static String extractSection(String content) {
        //log.info("extractSection content: {}", content);
        String startTag = "[:en]";
        String endTag = "[:]";

        int startIdx = content.indexOf(startTag);
        int endIdx = content.indexOf(endTag, startIdx + startTag.length());

//        if (startIdx == -1) {
//            log.info("Start tag not found in content: {}", content);
//        }
//
//        if (endIdx == -1) {
//            log.info("End tag not found in content: {}", content);
//        }

        if (endIdx <= startIdx) {
            log.info("startIdx: {}, endIdx: {}", startIdx, endIdx);
            log.info("End tag is before or at the same position as start tag in content: {}", content);
        }

        if (startIdx == -1 || endIdx == -1 || endIdx <= startIdx) {
            throw new RuntimeException("Invalid format for Edjmiatsin section - content :" + content);
        }

        return content.substring(startIdx + startTag.length(), endIdx).trim();
    }

    public static void setPrevAndNextLinksToPages(List<BibleBookChapterPages> pages) {
        for (int i = 0; i < pages.size(); i++) {
            BibleBookChapterPages current = pages.get(i);

            if (i > 0) {
                current.setPrevLink(pages.get(i - 1).getUrl());
            }

            if (i < pages.size() - 1) {
                current.setNextLink(pages.get(i + 1).getUrl());
            }
        }
    }

    public static String rewriteChapterContentLinks(String content, Map<String, String> nameToNewUrlMap) {
        var pattern = Pattern.compile("<a\\s+href=\"(.*?)\"");
        var matcher = pattern.matcher(content);
        var updatedContent = new StringBuilder();

        while (matcher.find()) {
            String oldUrl = matcher.group(1);
            String name = URLUtil.extractSegmentBetweenLastTwoSlashes(oldUrl);
            String newUrl = nameToNewUrlMap.get(name);
            if (newUrl == null) {
                throw new RuntimeException("URL not found in map: (pageUrl->oldUrl) " + oldUrl);
            }

            // Replace only href value
            String updatedTag = matcher.group(0).replace(oldUrl, newUrl);
            matcher.appendReplacement(updatedContent, Matcher.quoteReplacement(updatedTag));
        }

        matcher.appendTail(updatedContent);
        return updatedContent.toString();
    }
}
