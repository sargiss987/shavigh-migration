package am.shavigh.dbmigration.util;

public class URLUtil {

    public static String extractSegmentBetweenLastTwoSlashes(String url) {
        if (url == null || url.isEmpty()) {
            return "";
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
}
