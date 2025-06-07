package am.shavigh.dbmigration.controller;

import am.shavigh.dbmigration.model.postgres.SaintsBehavior;
import am.shavigh.dbmigration.service.SaintsBehaviorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/migration")
public class SaintsBehaviorController {

    private final SaintsBehaviorService saintsBehaviorService;

    public SaintsBehaviorController(SaintsBehaviorService saintsBehaviorService) {
        this.saintsBehaviorService = saintsBehaviorService;
    }

    @GetMapping("/saints-behavior")
    public ResponseEntity<String> migrate() {
        var content = saintsBehaviorService.migrateSaintsBehavior();

        return ResponseEntity.ok(content);
    }
}