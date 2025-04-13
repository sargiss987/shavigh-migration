package am.shavigh.dbmigration.controller;

import am.shavigh.dbmigration.dto.MigrationResult;
import am.shavigh.dbmigration.model.mysql.Post;
import am.shavigh.dbmigration.service.MigrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MigrationController {

    private final MigrationService migrationService;

    public MigrationController(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @GetMapping
    public String home(Model model) {
        model.addAttribute("post", new Post());
        return "migration";
    }

    @PostMapping("/migrate")
    public String searchPosts(@RequestParam("postURL") String postURL,
                              @RequestParam("bookName") String bookName,
                              Model model) {
        var result = migrationService.migrate(postURL, bookName);
        model.addAttribute("result", result);
        return "migration";
    }
}
