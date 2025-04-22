package am.shavigh.dbmigration.util;

import am.shavigh.dbmigration.dto.MigrationResult;
import am.shavigh.dbmigration.model.mysql.Post;
import am.shavigh.dbmigration.service.MigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class MigrationValidator {

    private static final Logger log = LoggerFactory.getLogger(MigrationValidator.class);

    public static void validatePost(List<Post> postList) {
        if (postList.size() > 1) {
            throw new RuntimeException("Post : Not unique result");
        }

        if (postList.isEmpty()) {
            throw new RuntimeException("Post : Empty result");
        }
    }

    public static void validatePostName(String postName, MigrationResult result) {
        if (postName.isEmpty()) {
            result.setErrorMessage("Empty post name");
            log.info("Post name : {}", postName);
            throw new RuntimeException("Empty post name");
        }
    }
}
