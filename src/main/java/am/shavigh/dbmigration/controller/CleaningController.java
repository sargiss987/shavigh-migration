package am.shavigh.dbmigration.controller;

import am.shavigh.dbmigration.service.CleaningService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CleaningController {

    private final CleaningService cleaningService;

    public CleaningController(CleaningService cleaningService) {
        this.cleaningService = cleaningService;
    }

    @GetMapping("/clean/chapters")
    public void cleanBibleBookChapters() {
       cleaningService.clean();
    }

    @GetMapping("/clean/pages")
    public void cleanBibleBookChapterPages() {
        cleaningService.cleanPages();
    }
}
