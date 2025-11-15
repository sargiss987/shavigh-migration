package am.shavigh.dbmigration.controller;

import am.shavigh.dbmigration.dto.MigrationResult;
import am.shavigh.dbmigration.model.mysql.Post;
import am.shavigh.dbmigration.service.ChapterMigrationService;
import am.shavigh.dbmigration.service.MigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MigrationController {

    private static final Logger log = LoggerFactory.getLogger(MigrationController.class);
    private final MigrationService migrationService;
    private final ChapterMigrationService chapterMigrationService;

    public MigrationController(MigrationService migrationService, ChapterMigrationService chapterMigrationService) {
        this.migrationService = migrationService;
        this.chapterMigrationService = chapterMigrationService;
    }

    @GetMapping
    public String home(Model model) {
        model.addAttribute("post", new Post());
        return "migration";
    }

    @PostMapping("/migrate")
    public String searchPosts(@RequestParam("postURL") String postURL,
                              @RequestParam("bookName") String bookName,
                              @RequestParam("serialNumber") int serialNumber,
                              @RequestParam("bibleBookId") int bibleBookId,
                              Model model) {
        var result = migrationService.migrate(postURL, bookName, serialNumber, bibleBookId);
        model.addAttribute("result", result);
        return "migration";
    }

    @GetMapping("/chapter-migration")
    public String chapterMigration(Model model) {
        model.addAttribute("post", new Post());
        return "chapter-migration";
    }

    @PostMapping("/migrate-chapters")
    public String migrateChapters(@RequestParam("postURL") String postURL,
                                  @RequestParam("translationName") String translationName,
                                  Model model) {
        try {
            chapterMigrationService.migrate(postURL, translationName);
            log.info("SUCCEED!!!");
            var result = new MigrationResult();
            result.setSuccessMessage("SUCCEED!!!");
            model.addAttribute("result", result);
            return "chapter-migration";
        } catch (Exception e) {
            log.error("FAILED!!! - {}", e.getMessage());
            var result = new MigrationResult();
            result.setErrorMessage("FAILED!!! - " + e.getMessage());
            model.addAttribute("result", result);
            return "chapter-migration";
        }
    }
}
