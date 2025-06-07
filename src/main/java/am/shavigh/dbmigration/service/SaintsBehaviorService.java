package am.shavigh.dbmigration.service;

import am.shavigh.dbmigration.model.postgres.SaintsBehavior;
import am.shavigh.dbmigration.model.postgres.SaintsBehaviorSection;
import am.shavigh.dbmigration.model.postgres.SaintsBehaviorSectionPage;
import am.shavigh.dbmigration.repository.mysql.MysqlRepo;
import am.shavigh.dbmigration.repository.postgres.SaintsBehaviorRepo;
import jakarta.transaction.Transactional;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SaintsBehaviorService {

    private static final Logger log = LoggerFactory.getLogger(SaintsBehaviorService.class);

    private static final Map<String, String> sectionTitleMap = Map.ofEntries(
            Map.entry("Վարք սրբոց հունվար", "saintsbehavior/echmiadzin/january"),
            Map.entry("Վարք սրբոց փետրվար", "saintsbehavior/echmiadzin/february"),
            Map.entry("Վարք սրբոց մարտ", "saintsbehavior/echmiadzin/march"),
            Map.entry("Վարք սրբոց ապրիլ", "saintsbehavior/echmiadzin/april"),
            Map.entry("Վարք սրբոց մայիս", "saintsbehavior/echmiadzin/may"),
            Map.entry("Վարք սրբոց հունիս", "saintsbehavior/echmiadzin/june"),
            Map.entry("Վարք սրբոց հուլիս", "saintsbehavior/echmiadzin/july"),
            Map.entry("Վարք սրբոց օգոստոս", "saintsbehavior/echmiadzin/august"),
            Map.entry("Վարք սրբոց սեպտեմբեր", "saintsbehavior/echmiadzin/september"),
            Map.entry("Վարք սրբոց հոկտեմբեր", "saintsbehavior/echmiadzin/october"),
            Map.entry("Վարք սրբոց նոյեմբեր", "saintsbehavior/echmiadzin/november"),
            Map.entry("Վարք սրբոց դեկտեմբեր", "saintsbehavior/echmiadzin/december")
    );

    private final MysqlRepo mysqlRepo;
    private final SaintsBehaviorRepo saintsBehaviorRepo;
    private final TranslationService translationService;

    public SaintsBehaviorService(MysqlRepo mysqlRepo, SaintsBehaviorRepo saintsBehaviorRepo, TranslationService translationService) {
        this.mysqlRepo = mysqlRepo;
        this.saintsBehaviorRepo = saintsBehaviorRepo;
        this.translationService = translationService;
    }

    @Transactional
    public String migrateSaintsBehavior() {
        try {
            var post = mysqlRepo.findPageByPostName("%d5%be%d5%a1%d6%80%d6%84-%d5%bd%d6%80%d5%a2%d5%b8%d6%81").getFirst();
            String content = post.getPostContent();

            var saintsBehavior = new SaintsBehavior();
            saintsBehavior.setTitle(post.getPostTitle());
            saintsBehavior.setUrl("saintsbehavior/echmiadzin");

            List<SaintsBehaviorSection> sections = extractSections(content, saintsBehavior);
            saintsBehavior.setSections(sections);
            saintsBehavior.setStatus("publish");

            //saintsBehaviorRepo.save(saintsBehavior);
            return content;

        } catch (Exception ex) {
            log.error("Migration failed: {}", ex.getMessage(), ex);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // Rollback transaction
            return null;
        }
    }

    private List<SaintsBehaviorSection> extractSections(String content, SaintsBehavior saintsBehavior) {
        List<SaintsBehaviorSection> sections = new ArrayList<>();
        Pattern pattern = Pattern.compile(
                "\\[bg_collapse[^\\]]*expand_text=\\\"(.*?)\\\"[^\\]]*](.*?)\\[/bg_collapse]",
                Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String expandText = matcher.group(1).trim();
            String innerContent = matcher.group(2).trim();

            var section = new SaintsBehaviorSection();
            section.setTitle(expandText);
            section.setSaintsBehavior(saintsBehavior);
            section.setUrl(sectionTitleMap.get(expandText));

            var pages = extractPages(innerContent, sectionTitleMap.get(expandText), section);

            // Update inner content with modified links
            section.setContent(updateContentWithInternalLinks(innerContent, pages));
            section.setPages(pages);

            sections.add(section);
        }

        return sections;
    }

    private List<SaintsBehaviorSectionPage> extractPages(String html, String baseUrl, SaintsBehaviorSection section) {
        var doc = Jsoup.parse(html);
        var links = doc.select("a[href]");
        List<SaintsBehaviorSectionPage> pages = new ArrayList<>();

        for (var link : links) {
            String href = link.attr("href");
            href = href.replaceFirst("^https?:", "");
            String[] parts = href.split("/");
            String lastPart = "";

            for (int i = parts.length - 1; i >= 0; i--) {
                if (!parts[i].isBlank()) {
                    lastPart = parts[i];
                    break;
                }
            }

            if (lastPart.equals("սկզբնաղբիւրներ")) {
                lastPart = URLEncoder.encode(lastPart, StandardCharsets.UTF_8);
            }
            var pageListFromDb = mysqlRepo.findPageByPostName(lastPart);


            if (pageListFromDb.isEmpty()) {
                log.info("-----------------------------------------------------------------");
                log.info("Page not found in MySQL: " + lastPart);
                log.info("Page not found in MySQL: " + URLDecoder.decode(lastPart, StandardCharsets.UTF_8));
                log.info("-----------------------------------------------------------------");
                var page = new SaintsBehaviorSectionPage();
                page.setTitle("404 - Page Not Found");
                page.setUrl(baseUrl + "/404");
                page.setContent("This page is not available.");
                page.setSaintsBehaviorSection(section);
                pages.add(page);
                continue; // Skip if no page found in MySQL
            }

            var pageFromDb = pageListFromDb.getFirst();

            String decodedHref = URLDecoder.decode(lastPart, StandardCharsets.UTF_8);

            // 🔁 Translate from Armenian to English
            String translated = translationService.translateText(decodedHref, "hy", "en");

            // 🧹 Clean: remove all non-alphanumeric characters (keep only [a-zA-Z0-9])
            String cleanSlug = translated.replaceAll("[^a-zA-Z0-9]", "");

            var page = new SaintsBehaviorSectionPage();
            page.setTitle(stripLocaleMarkers(pageFromDb.getPostTitle()));
            page.setUrl(baseUrl + "/" + cleanSlug);
            page.setContent(stripLocaleMarkers(pageFromDb.getPostContent()));
            page.setStatus("publish");
            page.setSaintsBehaviorSection(section);

            pages.add(page);
        }

        return pages;
    }

    private String updateContentWithInternalLinks(String html, List<SaintsBehaviorSectionPage> pages) {
        var doc = Jsoup.parse(html);
        var links = doc.select("a[href]");

        int index = 0;
        for (var link : links) {
            if (index >= pages.size()) {
                break; // No more pages to assign
            }

            var page = pages.get(index);
            link.attr("href", "/saintsbehavior/echmiadzin/page/" + page.getUrl());
            index++;
        }

        return doc.body().html();
    }

    private String stripLocaleMarkers(String content) {
        if (content == null) {
            return null;
        }

        String cleaned = content;

        // Remove leading '[:en]'
        if (cleaned.startsWith("[:en]")) {
            cleaned = cleaned.substring(5);
        }

        // Remove trailing '[:]'
        if (cleaned.endsWith("[:]")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned;
    }
}
