package am.shavigh.dbmigration.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class URLUtil {

    private static final Logger log = LoggerFactory.getLogger(URLUtil.class);

    public static String extractSegmentBetweenLastTwoSlashes(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        if (!url.endsWith("/")) {
            url += "/";
        }

        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return "";
        }

        int penultimateSlashIndex = url.lastIndexOf('/', lastSlashIndex - 1);
        if (penultimateSlashIndex == -1) {
            return "";
        }

        return url.substring(penultimateSlashIndex + 1, lastSlashIndex);
    }

    public static List<String> extractUrlsFromContent(String content) {
        var urls = new ArrayList<String>();
        var pattern = Pattern.compile("<a\\s+href=\"(.*?)\"");
        var matcher = pattern.matcher(content);

        while (matcher.find()) {
            log.info("DEBUG ::: extractUrlsFromContent {}", matcher.group(1));
            urls.add(matcher.group(1));
        }

        return urls;
    }

    public static List<String> extractUniqueNamesFromUrlList(String content) {
        var urls = extractUrlsFromContent(content);
        return urls.stream().map(URLUtil::extractSegmentBetweenLastTwoSlashes)
                .toList();
    }
}
