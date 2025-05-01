package am.shavigh.dbmigration.util;

import am.shavigh.dbmigration.dto.MigrationResult;
import am.shavigh.dbmigration.model.mysql.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class MigrationValidator {

    public static List<String> emptyPostUrlList = List.of("%d5%a3%d5%ac%d5%b8%d6%82%d5%ad-3036",
            "%d5%a5%d5%ac%d5%ab%d6%81-%d5%a3%d6%80%d6%84%d5%ab-%d5%b4%d5%a5%d5%af%d5%b6%d5%b8%d6%82%d5%a9%d5%b5%d5%b8%d6%82%d5%b6-31%d6%8923",
            "%d5%a5%d5%ac%d5%ab%d6%81-%d5%a3%d6%80%d6%84%d5%ab-%d5%b4%d5%a5%d5%af%d5%b6%d5%b8%d6%82%d5%a9%d5%b5%d5%b8%d6%82%d5%b6-32%d6%8925-2");

    private static final Logger log = LoggerFactory.getLogger(MigrationValidator.class);

    public static void validatePost(List<Post> postList, String postName) {
        if (postList.size() > 1) {
            log.info("Post : Not unique result {}", postName);
            throw new RuntimeException("Post : Not unique result");
        }

        if (postList.isEmpty() && !emptyPostUrlList.contains(postName)) {
            log.info("Post : Empty result {}", postName);
            throw new RuntimeException("Post : Empty result " + postName);
        }
    }

    public static String validatePostName(String postName, MigrationResult result) {
        if (postName == null || postName.isBlank()) {
            result.setErrorMessage("Empty post name");
            log.info("Post name is empty or null");
            throw new RuntimeException("Empty post name");
        }

        // If it's already percent-encoded, we assume it's fine
        if (postName.contains("%")) {
            return postName;
        }

        // Check if it contains non-ASCII characters and encode if needed
        boolean hasNonAscii = postName.chars().anyMatch(c -> c > 127);
        if (hasNonAscii) {
            String encoded = URLEncoder.encode(postName, StandardCharsets.UTF_8);
            log.info("Post name '{}' was encoded to '{}'", postName, encoded);
            return encoded;
        }

        return postName;
    }

}
